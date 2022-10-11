package com.example.mylibrary.models

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class Student : RealmObject {
    @PrimaryKey
    var studentId : ObjectId = ObjectId.create()
    var firstName: String? = null
    var lastName: String? = null
    var age = 0
    var gender: String? = null
    var city: String? = null //Create getter and setter
}

class StudentImp(
    var studentId: String,
    var firstName: String,
    var lastName: String,
    var age: Int,
    var gender: String,
    var city: String
)