package me.fzzy.robofzzy4j

import me.fzzy.robofzzy4j.thread.IndividualTask
import me.fzzy.robofzzy4j.thread.Task
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import java.io.File
import java.util.*

object MessageScheduler {

    private var tempMessages: HashMap<IMessage?, Long> = hashMapOf()

    init {
        Task.registerTask(IndividualTask({
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

    fun clearTempMessage(message: IMessage) {
        val iter = tempMessages.iterator()
        while (iter.hasNext()) {
            val i = iter.next()
            if (i.key != null) {
                if (message == i.key) {
                    iter.remove()
                    break
                }
            }
        }
    }

    fun sendTempMessage(timeToStayMillis: Long, channel: IChannel, text: String): IMessage? {
        val msg = Funcs.sendMessage(channel, text)
        if (msg != null) {
            tempMessages[msg] = System.currentTimeMillis() + timeToStayMillis
            return msg
        }
        return null
    }

    fun sendTempEmbed(timeToStayMillis: Long, channel: IChannel, embed: EmbedObject): IMessage? {
        val msg = Funcs.sendEmbed(channel, embed)
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