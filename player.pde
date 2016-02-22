void movePlayer() {

  //everything that has to do with the player physics, except collisions.

  playerVX+=playeraX;
  playerX+=playerVX;

  // limitation of speed
  if (4<playerVX) {
    playerVX=4;
  }
  if (playerVX<-4) {
    playerVX=-4;
  }

  // define acceleration
  if (!keyPressed) {
    if (1<abs(playerVX)) {
      playerVX*=0.9;
    } else {
      playerVX=0;
    }
  }

  if (keyPressed && keyCode==LEFT ) {
    if (0<playerVX && playeraX==0) {
      playeraX=1; //slowly stop
    } else {
      playeraX=-2; // accelate speed
    }
  }
  if (keyPressed && keyCode==RIGHT ) {
    if (playerVX<0 && playeraX==0) {
      playeraX=-1; // slow stop
    } else {
      playeraX=2; // accelerate speed
    }
  }

  playerVY+=gravity*1.3;
  playerY+=playerVY;


  if (keyPressed && keyCode==UP &&  playerVY==0) {
    playerVY=-10; //height of jumps
  }



  println("animCount= "+animCount);
  println("playerVX= "+playerVX);

  // dipict player
  //player moves to the right
  if (playerVX>0 && playerVY==0 ) {
    playerPhase=1;
    animCount++;
    if (animCount >10 && animCount <20)
      playerPhase=2;
    if (animCount >20 && animCount <30)
      playerPhase=1;
    if (animCount >30 && animCount <40)
      playerPhase=3;
    if (animCount >40)
      animCount =0;

    //player moves to the left
  } else if (playerVX<0 && playerVY==0) {
    playerPhase=4;
    animCount++;
    if (animCount >10 && animCount <20)
      playerPhase=5;
    if (animCount >20)
      animCount =0;
  }

  //player is jumping up
  if (playerVX==0 && playeraX==0 && playerVY<0 ) {
    playerPhase=8;
  } else if (playerVX==0 && playeraX==0 && playerVY>0) {
    playerPhase=8;
  }

  //player is jumping up to the right
  if (playerVX>0 && playerVY<0 ) {
    playerPhase=7;

    //player is jumping down to the left
  } else if (playerVX<0 && playerVY>0) {
    playerPhase=6;
  }

  //player is jumping down to the right
  if (playerVX>0 && playerVY>0 ) {
    playerPhase=7;

    //player is jumping up the left
  } else if (playerVX<0 && playerVY<0) {
    playerPhase=6;
  }

  if (!keyPressed && playerVX==0 && playeraX==0 && playerVY==0) {
    playerPhase=0;
  }


  //collision with enemy
  for (int i = 0; i < gX.length; i++) {
    if (dist(playerX, playerY, gX[i], gY[i])<gDiameter)  playerPhase=9;
  }
}

void keyReleased() {
  playeraX = 0;
}
