void drawClock() {
  //draw big pointer
  //set clock position from the data achieved
  float clockX, clockY;
  clockX=clockPosX-screenLeftX+49;
  clockY=clockPosY-screenTopY+49;
  //draw big pointer
  pushMatrix();
  translate(clockX, clockY);
  rotate(radians(clockDeg));
  stroke(0);
  strokeWeight(2);
  line(0, 0, 0, -70);
  noStroke();
  popMatrix();
  //draw small pointer
  pushMatrix();
  translate(clockX, clockY);
  rotate(radians(clockDeg/12));
  stroke(0);
  strokeWeight(4);
  line(0, 0, 0, -40);
  noStroke();
  popMatrix();

  if (gameState==GAMERUNNING) clockDeg+=6/frameRate;

  //turn the clock the other way round when collision player and enemy

  for (int i = 0; i < gX.length; i++) {
    if (dist(playerX, playerY, gX[i], gY[i])<gDiameter)  clockDeg-=3;
  }
 // println("clockX= "+clockX);
 // println("clockY= "+clockY);
}
