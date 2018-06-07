package me.fzzy.eventvoter

import me.fzzy.eventvoter.thread.IndividualTask
import me.fzzy.eventvoter.thread.Task
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import java.io.File
import java.util.*

class MessageScheduler constructor(task: Task) {

    private var tempMessages: HashMap<IMessage?, Long> = hashMapOf()

    init {
        task.registerTask(IndividualTask({
            val iter = tempMessages.iterator()
            while (iter.hasNext()) {
                val i = iter.next()
                if (System.currentTimeMillis() > i.value) {
                    i.key?.delete()
                    iter.remove()
                }
            }
        }, 1, true))
    }

    fun sendTempMessage(timeToStayMillis: Long, channel: IChannel, text: String): IMessage? {
        val msg = Funcs.sendMessage(channel, text)
        if (msg != null) {
            tempMessages[msg] = System.currentTimeMillis() + timeToStayMillis
            return msg
        }
        return null
    }

    fun sendTempFile(timeToStayMillis: Long, channel: IChannel, file: File): IMessage? {
        val msg = Funcs.sendFile(channel, file)
        if (msg != null) {
            tempMessages[msg] = System.currentTimeMillis() + timeToStayMillis
            return msg
        }
        return null
    }
}