# MayShow
## Description
This is an app that building a connection between users, allowing them to start a live video stream and watch others’ recording/recorded streams. The whole structure is shown here.

![Image of appStructure]
(https://github.com/RutgersMobileAppClass/MayShow/blob/master/Project%20Report-4.jpg)

## API and Libraries
Basically, MayShow is based on Kickflip SDK and Vitamio SDK. Firebase was included to help us manage the user’s information.

For Kickflip SDK, we implements createNewUser(), loginUser(), startStream(), stopStream(), getStreams(), getStreamsByUser() functions from KickflipApiClient.

For Vitamio SDK, we implements setVideoPath() and setMediaController() from widget.VideoView.

## Test
  * For the first time, click sign up and input any valid username and password. If you've already created a account, just click sign in.
  * You can click Square button to refresh the list.
  * You can search videos of an exist username at the top of the screen in Square.
  * You can click a video to watch, and long click a video to delete.

## Contributors
Xuewei Li, Shi Wang, Junjie Feng, Ruotian Zhang from Rutgers University.

_**Xuewei Li:**_ 
  * Sign in & sign up activity related to create a new user and login by username, by implementing KickflipApiClient. 
  * Basic UI structure building.
  * Testing transmitting quality.
  * Write report, slide, readme.

_**Shi Wang:**_
  * Finding SDK.
  * CameraActivity related to start broadcasting by implementing Kickflip SDK.
  * StreamListFragment related to receive live videostream by implementing Vitamio SDK.
  * Write report, slide, readme.

_**Junjie Feng:**_
  * Managing user’s information by implementing Firebase. MyProfile fragment related to streamadapter by implementing Kickflip SDK. 
  * Testing and debugging the searching module and the register and login module.
  * Adding indication to code.
  * Write report, slide, readme.

_**Ruotian Zhang:**_
  * StreamListFragment related to listview adapter and sort stream by keyword and username by implementing Kickflip SDK.
  * Testing and debugging the LogIn and SignUp module. 
  * Write report, slide, readme.


