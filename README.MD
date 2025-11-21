# Chinese Speech Trainer

## This is a video game work in progress.

##Business Logic
#start activity
Show the start screen with help button for 3 seconds, then transition if button not tapped.
add a button to request permission.
check for permissions and move on

#main activity
get the players last score
get the dictionary entry for the score
start the listener
override the listen event to check if the word matched

if match, change text green
if not match, change text red
destroy the listener
wait one second, then continue the init of start listener

#win activity
when there are no words left in the dictionary, 
the player has won and will have a screen to look for the next version

#stretch goals
card graphics behind the text
lock screen functionality