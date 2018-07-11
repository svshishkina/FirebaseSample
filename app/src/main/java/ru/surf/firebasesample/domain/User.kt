package ru.surf.firebasesample.domain


class User(val id: String = "", val name: String, val photoUrl: String, val gender: String) {

    constructor() : this("", "", "", "")
}