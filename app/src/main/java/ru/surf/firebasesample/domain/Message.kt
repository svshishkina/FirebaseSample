package ru.surf.firebasesample.domain


class Message(val uid: String, val message: String, val timestamp: Long) {

    constructor() : this("", "", 0)
}