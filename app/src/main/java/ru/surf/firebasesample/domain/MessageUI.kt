package ru.surf.firebasesample.domain


class MessageUI(val id: String = "", val user: User, val message: String, val timestamp: Long) {

    constructor() : this("", User("", "", "", ""), "", 0)
}