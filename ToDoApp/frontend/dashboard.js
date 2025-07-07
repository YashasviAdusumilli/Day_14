// dashboard.js

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("accessToken");
    if (!token) return window.location.href = "index.html";

    const taskForm = document.getElementById("taskForm");
    const taskInput = document.getElementById("taskInput");
    const prioritySelect = document.getElementById("prioritySelect");
    const taskList = document.getElementById("taskList");
    const toast = document.getElementById("toast");
    const spinner = document.getElementById("spinner");

    // Fetch tasks on load
    fetchTasks();

    taskForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const title = taskInput.value.trim();
        const priority = prioritySelect.value;

        if (!title) return showToast("Please enter a task", true);

        spinner.classList.remove("hidden");

        try {
            const res = await fetch("http://localhost:8888/tasks", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ title, priority })
            });

            if (res.ok) {
                showToast("Task added successfully 🎉");
                taskInput.value = "";
                fetchTasks();
            } else {
                const errorText = await res.text();
                showToast(`Failed to add task: ${errorText}`, true);
            }
        } catch (err) {
            console.error(err);
            showToast("An error occurred", true);
        } finally {
            spinner.classList.add("hidden");
        }
    });

    async function fetchTasks() {
        spinner.classList.remove("hidden");
        try {
            const res = await fetch("http://localhost:8888/tasks", {
                headers: { "Authorization": `Bearer ${token}` }
            });

            const tasks = await res.json();
            renderTasks(tasks);
        } catch (err) {
            console.error("Failed to fetch tasks", err);
        } finally {
            spinner.classList.add("hidden");
        }
    }

    function renderTasks(tasks) {
        taskList.innerHTML = "";
        if (!tasks || tasks.length === 0) {
            taskList.innerHTML = `<p class="text-gray-600 text-center">No tasks yet.</p>`;
            return;
        }

        tasks.forEach(task => {
            const taskDiv = document.createElement("div");
            taskDiv.className = "p-4 bg-white rounded shadow flex justify-between items-center";

            taskDiv.innerHTML = `
                <div>
                    <h3 class="text-lg font-medium ${task.completed ? 'line-through text-gray-500' : ''}">
                        ${task.title}
                    </h3>
                    <p class="text-sm text-${getPriorityColor(task.priority)}-600">Priority: ${task.priority}</p>
                </div>
                <input type="checkbox" ${task.completed ? "checked" : ""} class="ml-4" onchange="toggleTaskStatus('${task._id.$oid}', this.checked)" />
            `;

            taskList.appendChild(taskDiv);
        });
    }

    window.toggleTaskStatus = async (id, completed) => {
        try {
            const res = await fetch(`http://localhost:8888/tasks/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ completed })
            });

            if (res.ok) {
                showToast("Task updated ✅");
                fetchTasks();
            } else {
                showToast("Failed to update task", true);
            }
        } catch (err) {
            console.error(err);
            showToast("Error updating task", true);
        }
    };

    function getPriorityColor(priority) {
        switch (priority) {
            case "High": return "red";
            case "Medium": return "yellow";
            case "Low": return "green";
            default: return "gray";
        }
    }

    function showToast(message, isError = false) {
        toast.textContent = message;
        toast.className = `fixed bottom-6 left-1/2 transform -translate-x-1/2 px-4 py-2 rounded shadow-lg ${
            isError ? "bg-red-500" : "bg-green-500"
        } text-white show`;

        toast.classList.remove("hidden");
        setTimeout(() => toast.classList.add("hidden"), 3000);
    }
});

function logout() {
    localStorage.removeItem("accessToken");
    window.location.href = "index.html";
}
