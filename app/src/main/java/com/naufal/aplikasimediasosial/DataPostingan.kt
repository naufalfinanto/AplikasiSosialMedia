package com.naufal.aplikasimediasosial

class DataPostingan {
    var postID:String?=null
    var postText:String?=null
    var postImageURL:String?=null
    var postPersonUID:String?=null

    constructor(postIDs:String, postTexts:String, postImageURLs:String, postPersonUIDs:String){
        this.postID=postIDs
        this.postText=postTexts
        this.postImageURL=postImageURLs
        this.postPersonUID=postPersonUIDs
    }
}