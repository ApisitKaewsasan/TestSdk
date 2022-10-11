package com.example.mylibrary

import android.util.Log
import com.example.mylibrary.models.Student
import com.example.mylibrary.models.StudentImp
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class StudentManager {

    fun addStudent(firstName: String, lastName: String, age: Int, gender: String, city: String) {
        realm!!.writeBlocking {
            copyToRealm(Student().apply {
                this.firstName = firstName
                this.lastName = lastName
                this.age = age
                this.gender = gender
                this.city = city
            })
        }
        Log.d("Realm", "Write is done")
    }

    fun getStudents(): List<StudentImp> {
        return convertResultToList(realm!!.query<Student>().find())
    }

    private fun convertResultToList(realResultsList: RealmResults<Student>): List<StudentImp> {
        val intList: ArrayList<StudentImp> = ArrayList()
        for (student in realResultsList) {
            intList.add(
                StudentImp(
                    student.studentId.toString(),
                    student.firstName!!,
                    student.lastName!!,
                    student.age,
                    student.gender!!,
                    student.city!!
                )
            )
        }
        return intList
    }


    fun getStudentById(studentId: String): List<StudentImp> {
        return convertResultToList(realm!!.query<Student>("studentId == $0",ObjectId.from(studentId)).find())
    }

    fun queryStudents(query: String): RealmResults<Student> {
        return realm!!.query<Student>(query).find()
    }

    fun deleteStudentById(studentId: String) {
        realm!!.writeBlocking {
            val writeTransactionItems = query<Student>("studentId == $0",ObjectId.from(studentId)).find()
            delete(writeTransactionItems)
        }
    }

    fun deleteAllStudents() {
        realm!!.writeBlocking {
            delete(query<Student>())
        }
    }

    fun updateStudent(studentId: String, name: String) {
        realm!!.writeBlocking {
            val writeTransactionItems = query<Student>("studentId == $0",ObjectId.from(studentId)).find()
            if(writeTransactionItems.size > 0) {
                findLatest(writeTransactionItems.first()).apply {
                    this?.let {
                        it.firstName = name
                    }
                }
            }
        }


    }

    fun CoroutineScope(){
// flow.collect() is blocking -- run it in a background context
        val job = kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
            // create a Flow from the Item collection, then add a listener to the Flow
            val itemsFlow = realm!!.query<Student>().asFlow()
            itemsFlow.collect { changes: ResultsChange<Student> ->
                when (changes) {

                    // UpdatedResults means this change represents an update/insert/delete operation
                    is UpdatedResults -> {
                        Log.d("Realm changes", "changes  ${changes}")
                        changes.insertions // indexes of inserted objects
                        changes.insertionRanges // ranges of inserted objects
                        changes.changes // indexes of modified objects
                        changes.changeRanges // ranges of modified objects
                        changes.deletions // indexes of deleted objects
                        changes.deletionRanges // ranges of deleted objects
                        changes.list // the full collection of objects
                    }
                    else -> {
                        // types other than UpdatedResults are not changes -- ignore them
                    }
                }
            }
        }
        job.start()
    }

    companion object {
        private var realm: Realm? = null
        private var instance: StudentManager? = null

        init {
            val config = RealmConfiguration.Builder(schema = setOf(Student::class)).build()
            realm = Realm.open(config)
        }

        fun getInstance(): StudentManager? {
            if (instance == null) instance = StudentManager()
            return instance
        }

    }
}