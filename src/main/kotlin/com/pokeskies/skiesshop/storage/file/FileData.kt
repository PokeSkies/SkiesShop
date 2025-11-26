package com.pokeskies.skiesshop.storage.file

import com.pokeskies.skiesshop.data.UserData
import java.util.*

class FileData {
    var userdata: HashMap<UUID, UserData> = HashMap()
    override fun toString(): String {
        return "FileData(userdata=$userdata)"
    }
}
