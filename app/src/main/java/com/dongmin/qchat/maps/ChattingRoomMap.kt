package com.dongmin.qchat.maps

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init
import com.dongmin.qchat.activities.Init.StaticData.basicGamePort
import com.dongmin.qchat.activities.Init.StaticData.basicIP
import com.dongmin.qchat.activities.Init.StaticData.userInfo
import com.dongmin.qchat.elements.ChattingRoomElement
import com.dongmin.qchat.elements.UserInfo
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.UIModifyAvailableListener
import com.vdurmont.emoji.EmojiManager
import org.json.JSONObject
import kotlin.Char
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.ArrayList





class ChattingRoomMap(context : Context, private val roomInfo : ChattingRoomElement, private val screenWidth : Int, private val screenHeight : Int) : SurfaceView(context), SurfaceHolder.Callback {


    constructor(context : Context) : this(context, ChattingRoomElement(), 0, 0) {
        println("사용하지 않음")
    }


    //소켓 전송을 위한
    private var callback : Callback? = null
    interface Callback {
        fun sendToServer(messageType : String, message : String)
        fun emptyRoom()
    }


    private var socket : Socket? = null
    private val tag = "ChattingRoomMap"
    private var startedOnce = false
    private val userList = HashMap<String, Character>()
    private var paintingThread = PaintingThread(context)
    private val emojiRegex = "(\\u00a9|\\u00ae|[\\u2000-\\u3300]|\\ud83c[\\ud000-\\udfff]|\\ud83d[\\ud000-\\udfff]|\\ud83e[\\ud000-\\udfff])"

    //각종 처리 이미지
    private val characterWidth = screenWidth / 8
    private val characterHeight = screenWidth / 8
    private val imgCharacter = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.img_lion), characterWidth, characterHeight, true)

    private val charWidth = imgCharacter.width
    private val charHeight = imgCharacter.height
    private val charCWidth = charWidth / 2
    private val charCHeight = charHeight / 2
    private val charX = (screenWidth / 2) - charCWidth
    private val charY = (screenHeight / 2) - charCHeight

    //플래그
    var isOX = false

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {

        Log.d("ChattingRoomMap", "surfaceCreated Called")
        //게임 스레드 시작
        paintingThread = PaintingThread(context)
        paintingThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d("ChattingRoomMap", "surfaceChanged Called")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d("ChattingRoomMap", "surfaceDestroyed Called")
        var done = true
        while(done) {
            paintingThread.interrupt()
            done = false
        }
    }

    fun setCallback(callback : Callback) {
        this.callback = callback
    }

    private inner class PaintingThread(context : Context) : Thread() {

        private val characterWidth = screenWidth / 8
        private val characterHeight = screenWidth / 8
        private val roomColorHeight = screenHeight / 3
        // TODO 얼굴 (private val faceFitConstant = 1.1f)

        private val imgCorrect = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ico_chat_correct), screenWidth / 9, screenWidth / 9, true)
        private val imgIncorrect = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ico_chat_incorrect), screenWidth / 9, screenWidth / 9, true)
        private val img3Dots = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ico_chat_three_dots), screenWidth / 9, screenWidth / 9, true)
        private val imgGoBattle = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ico_chat_go_battle), screenWidth / 9, screenWidth / 9, true)
        private val imgBattleWin = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ico_chat_battle_win), screenWidth / 9, screenWidth / 9, true)

        private var canvas : Canvas? = null
        private val srcCharacter = Rect(0, 0, characterWidth, characterHeight)

        override fun run() {
            Log.e(tag, "Game Thread Started")

            srcCharacter.set(0, 0, characterWidth, characterHeight)
            while(true) {
                canvas = holder.lockCanvas()
                try {
                    synchronized(holder) {
                        //방을 흰색으로 칠한다
                        canvas!!.drawRGB(255, 255, 255)

                        //아직 어디쓰는 물건인지 모르겠다
                        val qBoard = Paint()
                        qBoard.color = Color.rgb(240, 240, 240)
                        qBoard.style = Paint.Style.FILL

                        //아래 초록 배경을 그린다
                        val room = Paint()
                        room.color = Color.rgb(234, 252, 151)
                        room.style = Paint.Style.FILL
                        canvas!!.drawRect(0f, roomColorHeight.toFloat(), screenWidth.toFloat(), screenHeight.toFloat(), room)

                        if(isOX) {
                            //TODO(OX 맵그리기)
                        }

                        try {
                            val textPaint = TextPaint()
                            textPaint.textSize = determineAppropriateTextSize()
                            textPaint.isAntiAlias = true
                            //각 유저에 대해 그린다
                            val it = userList.keys.iterator()
                            while(it.hasNext()) {
                                val user = userList[it.next()]!!
                                //캐릭터 위치 잡기
                                val src = Rect()
                                val dst = Rect()
                                src.set(0, 0, characterWidth, characterHeight)
                                if(user.isMoving) {
                                    val ultX = (user.position.x) +
                                            (user.destination!!.x - user.position.x) / user.movingProcedureComplete
                                    val ultY = (user.position.y) +
                                            (user.destination!!.y - user.position.y) / user.movingProcedureComplete
                                    dst.set(ultX - characterWidth / 2, ultY - characterHeight / 2, ultX + characterWidth / 2, ultY + characterHeight / 2)
                                    user.position.set(ultX, ultY)
                                    user.addMovingProcedure()
                                } else {
                                    dst.set(user.position.x - characterWidth / 2, user.position.y - characterHeight / 2,
                                        user.position.x + characterWidth / 2, user.position.y + characterHeight / 2)
                                }

                                //얼굴 그리기
                                if(user.isFacable) {
                                    //TODO (얼굴 기능 추가할 때 작성할 것)
                                }

                                //캐릭터 몸체 그리기
                                try {
                                    canvas!!.drawBitmap(user.characterImg!!, src, dst, null)
                                } catch(e3 : NullPointerException) {
                                    Log.e(tag, "캐릭터 몸체 정보 식별 불가")
                                }

                                //닉네임 그리기
                                val nicknamePoint = user.position
                                val nicknameWidth = textPaint.measureText(user.userInfo.userNickname)
                                try {
                                    canvas!!.drawText(user.userInfo.userNickname, nicknamePoint.x - nicknameWidth / 2f, nicknamePoint.y + screenHeight / 16f, textPaint)
                                } catch(e3: NullPointerException) {
                                    Log.e(tag, "닉네임 정보 식별 불가")
                                }

                                //말풍선 그리기
                                if(user.isTalking) {
                                    val userPosX = user.position.x
                                    val userPosY = user.position.y
                                    val specialBalloonSrc = Rect()
                                    specialBalloonSrc.set(0, 0, screenWidth / 9, screenWidth / 9)
                                    val specialBalloonDst = Rect()
                                    specialBalloonDst.set(userPosX - screenWidth / 18, userPosY - screenWidth / 9 - screenHeight / 30, userPosX + screenWidth / 18, userPosY - screenHeight / 30)

                                    when(user.talkingContent) {
                                        //특수 상황에서 나타나는 말풍선에 대한 이미지 처리
                                        "A_SDM__CODE_CORRECT_ANSWER" -> {
                                            canvas!!.drawBitmap(imgCorrect, specialBalloonSrc, specialBalloonDst, null)
                                        }
                                        "A_SDM__CODE_WRONG_ANSWER" -> {
                                            canvas!!.drawBitmap(imgIncorrect, specialBalloonSrc, specialBalloonDst, null)
                                        }
                                        "A_SDM__CODE_WEIRED" -> {
                                            canvas!!.drawBitmap(img3Dots, specialBalloonSrc, specialBalloonDst, null)
                                        }
                                        "A_SDM__CODE_GO_BATTLE" -> {
                                            canvas!!.drawBitmap(imgGoBattle, specialBalloonSrc, specialBalloonDst, null)
                                        }
                                        "A_SDM__CODE_BATTLE_WIN" -> {
                                            canvas!!.drawBitmap(imgBattleWin, specialBalloonSrc, specialBalloonDst, null)
                                        }
                                        else -> {
                                            //입력한 텍스트의 줄을 나눠서 각 줄마다 따로 저장, 각 줄을 저장한 리스트를 choppedString 이라고 한다
                                            val choppedString = ArrayList<String>()
                                            var knapsacks = 0
                                            //var rocks = 0
                                            var lastRead = 0

                                            var i = 0
                                            while(i < user.talkingContent.length) {
                                                if(textPaint.measureText(user.talkingContent.substring(lastRead, i)) > screenWidth / 3) {
                                                    if(EmojiManager.isEmoji(user.talkingContent.substring(i - 1, i + 1))) {
                                                        i ++
                                                    }
                                                    choppedString.add(user.talkingContent.substring(lastRead, i))
                                                    lastRead = i
                                                    knapsacks ++
                                                }

                                                /* 기존 문자열 나누기 알고리즘. 이모지가 제대로 표시되지 않는 문제가 있었다.
                                                if(user.talkingContent[i] < 128.toChar()) {
                                                    rocks ++
                                                } else {
                                                    rocks += 2
                                                }
                                                if(rocks > 18) {
                                                    choppedString.add(user.talkingContent.substring(lastRead, i))
                                                    lastRead = i
                                                    rocks = 0
                                                    knapsacks ++
                                                }
                                                */
                                                if(i == user.talkingContent.length - 1) {

                                                    choppedString.add(user.talkingContent.substring(lastRead, user.talkingContent.length))
                                                    break
                                                }
                                                i ++
                                            }

                                            //말풍선 제작

                                            //ascent 는 베이스라인 위로의 텍스트 길이, descent 는 베이스라인 아래로의 텍스트 길이, 결국 합치면 글자의 높이다.
                                            val perRowHeight = textPaint.ascent() + textPaint.descent() + screenWidth / 12

                                            //줄 중에서 가장 긴 너비를 가지는 것을 택한다.
                                            var maxRowWidth = textPaint.measureText(choppedString[0])
                                            for(j : Int in 1..(choppedString.size - 1)) {
                                                if(textPaint.measureText(choppedString[j]) > maxRowWidth) {
                                                    maxRowWidth = textPaint.measureText(choppedString[j])
                                                }
                                            }

                                            //말풍선 꼬리의 경로 결정
                                            val balloonTailPoint1 = Point()
                                            val balloonTailPoint2 = Point()
                                            val balloonTailPoint3 = Point()
                                            balloonTailPoint1.set(userPosX - screenWidth / 60, userPosY - screenHeight / 20)
                                            balloonTailPoint2.set(userPosX + screenWidth / 60, userPosY - screenHeight / 20)
                                            balloonTailPoint3.set(userPosX, userPosY - screenHeight / 30)

                                            val balloonTailPath = Path()
                                            balloonTailPath.fillType = Path.FillType.EVEN_ODD
                                            balloonTailPath.moveTo(balloonTailPoint1.x.toFloat(), balloonTailPoint1.y.toFloat())
                                            balloonTailPath.lineTo(balloonTailPoint3.x.toFloat(), balloonTailPoint3.y.toFloat())
                                            balloonTailPath.lineTo(balloonTailPoint2.x.toFloat(), balloonTailPoint2.y.toFloat())
                                            balloonTailPath.close()

                                            val balloonConcatPath = Path()
                                            balloonConcatPath.fillType = Path.FillType.EVEN_ODD
                                            balloonConcatPath.moveTo(balloonTailPoint1.x.toFloat(), balloonTailPoint1.y.toFloat() - 3f)
                                            balloonConcatPath.lineTo(balloonTailPoint2.x.toFloat(), balloonTailPoint2.y.toFloat() - 3f)
                                            balloonConcatPath.lineTo(balloonTailPoint2.x.toFloat(), balloonTailPoint2.y.toFloat() + 2f)
                                            balloonConcatPath.lineTo(balloonTailPoint1.x.toFloat(), balloonTailPoint1.y.toFloat() + 2f)
                                            balloonConcatPath.close()

                                            //말풍선 그리기
                                            val balloonBodyPaint = Paint()
                                            val balloonStrokePaint = Paint()
                                            val balloonStrokeWhitePaint = Paint()
                                            balloonBodyPaint.isAntiAlias = true
                                            balloonStrokePaint.isAntiAlias = true
                                            balloonStrokePaint.style = Paint.Style.STROKE

                                            balloonBodyPaint.color = Color.parseColor("#ffffff")
                                            balloonStrokePaint.color = Color.parseColor("#000000")
                                            balloonStrokeWhitePaint.color = Color.parseColor("#ffffff")

                                            //말풍선 몸체와 테두리
                                            balloonStrokePaint.strokeWidth = 3f
                                            canvas!!.drawRect(userPosX - maxRowWidth / 2 - screenWidth / 30,
                                                userPosY - choppedString.size * perRowHeight - screenHeight / 35 - screenWidth / 20,
                                                userPosX + maxRowWidth / 2 + screenWidth / 30,
                                                userPosY - screenHeight / 50f - screenWidth / 20,
                                                balloonBodyPaint)
                                            canvas!!.drawRect(userPosX - maxRowWidth / 2 - screenWidth / 30,
                                                userPosY - choppedString.size * perRowHeight - screenHeight / 35 - screenWidth / 20,
                                                userPosX + maxRowWidth / 2 + screenWidth / 30,
                                                userPosY - screenHeight / 50f - screenWidth / 20,
                                                balloonStrokePaint)

                                            //말풍선 꼬리
                                            balloonStrokePaint.strokeWidth = 6f
                                            canvas!!.drawPath(balloonTailPath, balloonStrokePaint)
                                            canvas!!.drawPath(balloonTailPath, balloonBodyPaint)
                                            canvas!!.drawPath(balloonConcatPath, balloonStrokeWhitePaint)
                                            canvas!!.drawPath(balloonConcatPath, balloonBodyPaint)


                                            //텍스트 출력
                                            for(j : Int in 0..(choppedString.size - 1)) {
                                                canvas!!.drawText(choppedString[j], userPosX - textPaint.measureText(choppedString[j]) / 2,
                                                    userPosY - screenHeight / 35 - (choppedString.size - j) * perRowHeight, textPaint)
                                            }
                                        }
                                    }
                                    user.addTalkingProcedure()
                                }
                            }
                        } catch(e2 : IndexOutOfBoundsException) {
                            Log.d(tag, "프레임 표현 유저수와 실제 유저수의 불일치")
                        }
                    }
                } catch(e1 : Exception) {
                    Log.e(tag, "프레임 표현 에러")
                    e1.printStackTrace()
                    return
                }

                try {
                    holder.unlockCanvasAndPost(canvas!!)
                } catch(e1 : Exception) {
                    Log.e(tag, "프레임 마무리 에러")
                }

                try {
                    Thread.sleep(30)
                } catch(e1 : Exception) {
                    Log.e(tag, "프레임 중단 에러")
                    return
                }
            }
        }


        //최초로 TextPaint 에 설정되는 텍스트 사이즈가 작으므로, "ㅇㅇㅇㅇㅇㅇㅇㅇㅇ" 라는 텍스트가 화면 가로길이의 1/3을 차지할 정도의 텍스트
        //크기를 선정하여 반환한다.
        private fun determineAppropriateTextSize(): Float {
            var size = 0
            val paint = Paint()
            do {
                paint.textSize = (++size).toFloat()
            } while (paint.measureText("ㅇㅇㅇㅇㅇㅇㅇㅇㅇ") < screenWidth / 3)
            return size.toFloat()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        if (event.action == MotionEvent.ACTION_DOWN) {
            try {
                val user = userList[userInfo.userNo.toString()]!!
                val x = event.x.toInt()  // 클릭한 위치를 Rect()로 만듦
                val y = event.y.toInt()
                // room_members.get(i).setPosition(x, y);
                user.setDestination(x, y)
                user.isMoving = true
                Thread(Runnable {
                    try {
                        callback!!.sendToServer("2-002", roomInfo.roomId + ":;:" + user.userInfo.userNo + ":;:" + (user.destination!!.x).toDouble() / screenWidth + ":;:" +  (user.destination!!.y).toDouble() / screenHeight)

                    } catch (e: IOException) {
                        Log.e(tag, "캐릭터 움직임 생성 에러")
                    }
                }).start()
            } catch (e1: IndexOutOfBoundsException) {
                Log.e(tag, "캐릭터 움직임 에러")
            } catch (e2 : NullPointerException) {
                callback!!.emptyRoom()
            }
        }
        return true
    } // touch

    override fun performClick() : Boolean {
        super.performClick()
        return true
    }

    private inner class Character(usrInfo : UserInfo) {

        //각종 플래그
        var isTalking: Boolean = false  //말풍선
        var isMoving: Boolean = false   //움직임
        var isFacable = false           //얼굴 사진이 들어가는 캐릭터
        var isSizeSet = false           //아직 뭔지 모르겠음

        //현재 위치와 목표 위치(isTalking 변수가 true 인 경우 사용)
        var position: Point = Point(screenWidth / 2, screenHeight / 2)
        var destination: Point? = null

        //캐릭터에 대응되는 유저정보 클래스
        val userInfo: UserInfo = usrInfo

        //캐릭터 및 얼굴이 들어가는 캐릭터의 경우 얼굴이미지도 로드한다
        var characterPath : String = ""
        var characterImg: Bitmap? = null
        var faceImg: Bitmap? = null

        //캐릭터의 움직임을 분할한다
        var movingProcedure: Int = 0
        var movingProcedureComplete: Int = 0

        //말풍선의 움직임을 분할한다
        var talkingProcedure: Int = 0
        var talkingProcedureComplete: Int = 0
        var talkingContent: String = ""

        //얼굴이 들어가는 캐릭터이미지에 대한 얼굴위치 처리를 한다
        var faceposX: Int = 0
        var faceposY: Int = 0
        var faceWidth: Int = 0
        var faceHeight: Int = 0
        var wholeWidth: Int = 0
        var wholeHeight: Int = 0

        init {
            //캐릭터 이미지 삽입
            HttpConnector("character/fetch_character_path_table.php", "", object : UIModifyAvailableListener {
                override fun taskCompleted(result: String?) {
                    Thread(Runnable {
                        var connection: HttpURLConnection? = null
                        try {
                            val data = JSONObject(result).getJSONArray("characters_list")
                            for(i : Int in 0..(data.length() - 1)) {
                                if(data.getJSONObject(i).getString("character_no") == userInfo.userNowUsingCharacter.toString()) {
                                    //val url = URL("http://dak2183242.cafe24.com/qchat/character/" + data.getJSONObject(i).getString("path"))
                                    characterImg = Bitmap.createScaledBitmap(Glide.with(context).asBitmap().load("http://dak2183242.cafe24.com/qchat/character/" + data.getJSONObject(i).getString("path")).submit().get() , screenWidth / 8, screenWidth / 8, true)
                                    /*
                                    connection = url.openConnection() as HttpURLConnection
                                    connection.doInput = true
                                    connection.connect()
                                    val input = connection.inputStream
                                    characterImg = BitmapFactory.decodeStream(input)
                                    characterImg = Bitmap.createScaledBitmap(characterImg!!, screenWidth / 8, screenWidth / 8, true)
                                    */
                                    break
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            connection?.disconnect()
                        }
                    }).start()
                }
            }).execute()
        }

        fun addMovingProcedure() {
            this.movingProcedure++
            if (movingProcedure >= movingProcedureComplete) {
                this.movingProcedure = 0
                this.movingProcedureComplete = 1
                this.isMoving = false
            }
        }

        fun addTalkingProcedure() {
            this.talkingProcedure++
            if (talkingProcedure >= talkingProcedureComplete) {
                this.talkingProcedure = 0
                this.talkingProcedureComplete = 1
                this.talkingContent = ""
                this.isTalking = false
            }
        }

        fun setDestination(x: Int, y: Int) {
            val point = Point()
            if (y < screenHeight / 3) {
                point.set(x, screenHeight / 3)
            } else {
                point.set(x, y)
            }
            this.destination = point
            movingProcedureComplete = generateMovingProcedure(position, point)
        }

        fun generateMovingProcedure(src: Point, dst: Point): Int {
            var partition = 0
            this.isMoving = true
            Log.d(tag,"srcX: " + src.x + ", dstX: " + dst.x + ", srcY: " + src.y + ", dstY: " + dst.y)
            val length =
                Math.sqrt((src.x.toDouble() / screenWidth.toDouble() - dst.x.toDouble() / screenWidth.toDouble()) * (src.x.toDouble() / screenWidth.toDouble() - dst.x.toDouble() / screenWidth.toDouble())
                        + (src.y.toDouble() / screenHeight.toDouble() - dst.y.toDouble() / screenHeight.toDouble()) * (src.y.toDouble() / screenHeight.toDouble() - dst.y.toDouble() / screenHeight.toDouble()))
            partition = (length / 0.01).toInt() //경로 분할
            if (partition == 0) partition = 1 // div/0 방지
            return partition
        }

        fun generateTalkingProcedure(message : String) {
            this.isTalking = true
            this.talkingContent = message
            this.talkingProcedure = 0
            this.talkingProcedureComplete = 100
        }
    }

    //채팅룸맵은 2번으로 한다
    //2-001 (상대 캐릭터 입장)
    //2-002 (움직임) 2-002:;:roomId:;:userNo:;:DestinationX:;:DestinationY
    private inner class ClientSender(val messageType : String, val message : String) : Thread() {
        override fun run() {
            try {
                val socket = Socket(basicIP, basicGamePort)
                val out = DataOutputStream(socket.getOutputStream())

                out.writeUTF("$messageType:;:$message")
                Log.i("ClientSender $messageType", message)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun chattingListTouch(event : MotionEvent) {
        onTouchEvent(event)
    }


    fun exit(userNo : String) {
        userList.remove(userNo)
    }

    fun talk(userNo : String, message : String) {
        userList[userNo]!!.generateTalkingProcedure(message)
    }

    fun addCharacter(userInfo : UserInfo) {
        userList[userInfo.userNo.toString()] = Character(userInfo)
    }

    fun addExistCharacter(userInfo : UserInfo, positionX : Float, positionY : Float) {
        val character = Character(userInfo)
        character.position.x = (positionX * screenWidth).toInt()
        character.position.y = (positionY * screenHeight).toInt()
        userList[userInfo.userNo.toString()] = character
    }

    fun moveCharacter(userNo : String, destinationX : Float, destinationY : Float) {
        val user = userList[userNo]!!
        user.setDestination((destinationX * screenWidth).toInt(), (destinationY * screenHeight).toInt())
    }

    fun destroyMap() {
        paintingThread.interrupt()
    }

    fun chattingRoomPaused() {
        paintingThread.interrupt()
    }
}