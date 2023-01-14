package gg.HmZyy.fancy_video_player.utils
import java.io.Serializable

class Subtitle : Serializable {
    var url: String
    var label: String

    constructor(url: String, label: String) {
        this.url = url
        this.label = label
    }
}