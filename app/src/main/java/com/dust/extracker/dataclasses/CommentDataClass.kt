package com.dust.extracker.dataclasses

data class CommentDataClass(var id:Int,
                            // viewtype 0 = comment
                            // viewtype 1 = reply
                            var viewtype:Int,
                            var userName:String,
                            var commentContent:String,
                            var date:String,
                            var avatarImageUrl:String,
                            var likesCount:Int,
                            var disslikesCount:Int
                            ) {
}