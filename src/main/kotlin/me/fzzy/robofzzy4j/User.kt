package me.fzzy.robofzzy4j

import com.koloboke.collect.impl.hash.Hash
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import kotlin.math.roundToInt

class User private constructor(val id: Long) {

    companion object {

        private val users: ArrayList<User> = arrayListOf()

        fun getUser(id: Long): User {
            for (user in users) {
                if (user.id == id)
                    return user
            }
            val user = User(id)
            users.add(user)
            return user
        }

        fun getUser(user: IUser): User {
            return getUser(user.longID)
        }
    }

    private val cooldowns: HashMap<String, Cooldown> = hashMapOf()

    var runningCommand = false

    fun getCooldown(category: String): Cooldown {
        if (!cooldowns.containsKey(category)) cooldowns[category] = Cooldown()
        return cooldowns[category]!!
    }

    fun getAllCooldowns(): HashMap<String, Cooldown> {
        return cooldowns
    }

    fun getCooldownModifier(guild: Guild): Int {
        val rank = guild.leaderboard.getRank(id)
        val division = (1.0 / 90.0) * (guild.getZeroRank() - 1)
        if (rank != null) {
            // https://www.desmos.com/calculator/hfacvqhzic
            var reduce = 0.0
            if (division != 0.0)
                reduce = -((rank - 1) / division) + 90

            if (reduce < 0)
                reduce = 0.0
            return reduce.roundToInt()
        }
        return 0
    }

}