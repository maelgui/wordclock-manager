package fr.maelgui.wordclockmanager

class WordclockMessage(val error: Error, val command: Command, val length: Int, val message: ArrayList<Int>) {
    enum class Error {
        NONE,
        BAD_PACKET,
        UNKNOWN_COMMAND,
        NOT_IMPLEMENTED;

        companion object {
            private val VALUES = values();
            fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
        }
    }

    enum class Command {
        ERROR,
        VERSION,
        // RTC
        TIME,
        TEMPERATURE,
        HUMIDITY,
        LIGHT,
        TEMPERATURES,
        // Function related
        TIMER,
        // Settings
        MODE,
        FUNCTION,
        BRIGHTNESS,
        THRESHOLD,
        ROTATION,
        NONE;

        companion object {
            private val VALUES = values();
            fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 16
    }

    class Builder(val command: Command) {
        val message = ArrayList<Int>()

        fun build(): WordclockMessage {
            val msg = WordclockMessage(Error.NONE, command, message.size, message)
            return msg
        }

        fun setByte(byte: Int): WordclockMessage.Builder {
            message.add(byte)
            return this
        }
    }

    override fun toString(): String {
        return "error: $error; command: $command; length: $length; msg: $message"
    }
}