package com.example
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

// --- 1. Data Modeling ---
@Serializable
data class Task(val id: Int, var content: String, var isDone: Boolean)

@Serializable
data class TaskRequest(val content: String, val isDone: Boolean)

// --- 2. Data Layer (In-Memory) ---
object TaskRepository {
    private val idCounter = AtomicInteger()
    private val tasks = mutableListOf(
        Task(idCounter.getAndIncrement(), "เรียนรู้ Ktor", true),
        Task(idCounter.getAndIncrement(), "สร้าง REST API", false),
        Task(idCounter.getAndIncrement(), "ทดสอบ Endpoint", false)
    )

    fun getAll(): List<Task> = tasks
    fun getById(id: Int): Task? = tasks.find { it.id == id }
    fun add(taskRequest: TaskRequest): Task {
        val newTask = Task(
            id = idCounter.getAndIncrement(),
            content = taskRequest.content,
            isDone = taskRequest.isDone
        )
        tasks.add(newTask)
        return newTask
    }
    fun update(id: Int, taskRequest: TaskRequest): Task? {
        val task = getById(id)?.also {
            it.content = taskRequest.content
            it.isDone = taskRequest.isDone
        }
        return task
    }
    fun delete(id: Int): Boolean = tasks.removeIf { it.id == id }
}


// --- 3. Routing ---
fun Application.configureRouting() {
    routing {
        get {
            call.respondText { "Hello ปุณยภา กรกฏกำจร!" }
        }
        // GET /tasks: คืนค่า task ทั้งหมด
        get("/tasks") {
            val allTasks = TaskRepository.getAll()
            call.respond(HttpStatusCode.OK, allTasks)
        }

        // GET /tasks/{id}: คืนค่า task แค่ตัวเดียว
        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }
            TaskRepository.getById(id)?.let { task ->
                call.respond(HttpStatusCode.OK, task)
            } ?: call.respond(HttpStatusCode.NotFound, "Task with ID $id not found")
        }

        // POST /tasks: สร้าง task ใหม่
        post("/tasks") {
            try {
                val taskRequest = call.receive<TaskRequest>()
                val newTask = TaskRepository.add(taskRequest)
                call.respond(HttpStatusCode.Created, newTask)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            }
        }

        // PUT /tasks/{id}: อัปเดต task ที่มีอยู่
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@put
            }
            try {
                val taskRequest = call.receive<TaskRequest>()
                TaskRepository.update(id, taskRequest)?.let { updatedTask ->
                    call.respond(HttpStatusCode.OK, updatedTask)
                } ?: call.respond(HttpStatusCode.NotFound, "Task with ID $id not found")
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            }
        }

        // DELETE /tasks/{id}: ลบ task
        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@delete
            }
            if (TaskRepository.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Task with ID $id not found")
            }
        }

    }

}