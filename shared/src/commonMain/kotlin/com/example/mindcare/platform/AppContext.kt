package com.example.mindcare.platform

expect abstract class AppContext

object ContextHolder {
    lateinit var context: AppContext
}
