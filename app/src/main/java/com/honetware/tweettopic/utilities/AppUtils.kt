package com.honetware.tweettopic.utilities

import com.honetware.tweettopic.model.ImageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import twitter4j.Status
import twitter4j.Twitter

class AppUtils{
    companion object{
        fun getStatus(twitter: Twitter, tweetId: Long){
            val status = twitter.showStatus(tweetId)
        }

        suspend fun getStatusContent(status: Status): ImageContent{


            //get status owner
            val user = withContext(Dispatchers.IO){status.user}
            val profileUrl = user.profileImageURL
            val name = user.name
            val userName = user.screenName
            val statusText = status.text
            val date = status.createdAt
            val favCount = status.favoriteCount
            val retweetCount = status.retweetCount

            //get if has media entity
            val mediaEntity = status.mediaEntities

            return ImageContent(profileUrl,name,userName,statusText,mediaEntity,favCount,retweetCount,date)
        }
    }
}