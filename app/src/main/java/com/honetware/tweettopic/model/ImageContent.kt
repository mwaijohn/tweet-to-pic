package com.honetware.tweettopic.model

import twitter4j.MediaEntity
import java.util.*

class ImageContent(
    var userProfile: String,
var name: String,
var userName: String,
var statusText: String?,
var imageEntities: Array<MediaEntity>?,
var favCount: Int,
var retweetCount: Int,
var date: Date
) {
}