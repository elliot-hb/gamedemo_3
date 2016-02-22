Map map;

//player with and height
int playerW = 100;
int playerH = 200;


float playerX; // position of playerX
float playerY; // position of playerY
//color pC; // color of player

float clockDeg;
int clockSecs;

float playerVX; //speed along x-axis
float playerVY; //speed along y-axis
float playeraX; //acceleration along x-axis

//counter used for player animation;
int animCount;

float clockPosX; //Coordinates of Clock, that will be set during newGame() from position of tile 'A'
float clockPosY; //

float gravity=0.5; // define gravity
float floorHeight;

float [] gX;  //position of enemy on x axis
float [] gVX; //enemy velocity on x axis
float [] gY; //position of enemy on y axis
char [] enemyStartTileName = {'X', 'Y', 'Z'}; //list of the names of the tiles where enemies appear, add more tilenames, to get more enemies, but also increase the numOfEnemies then!
int numOfEnemies = 3; //numberOfEnemies
int gDiameter=56; //enemy diamete
float huntingSpeed = 0.95; //increasing this will let the enemy hunt the player faster
color gC;


//counts the flowers, to set when win
int counter;

// Whether to illustrate special functions of class Map
boolean showSpecialFunctions=false;

// left / top border of the screen in map coordinates
// used for scrolling
float screenLeftX, screenTopY;

float time;
int GAMEWAIT=0, GAMERUNNING=1, GAMEOVER=2, GAMEWON=3;
int gameState;

PImage backgroundImg;
PImage P;

////declare classes
//GreyMan myGreyMen;

///////// Loads a set of numbered images ///////////////
// filenames is a relative filename with TWO 00s
// e.g. images/fox-00.png. The function then tries
// to load images/fox-00.png, images/fox-01.png, ..
// as long as these files exist.
ArrayList<PImage> loadImages (String filePattern) {
  // Count number of question marks
  String qmString="";
  while (filePattern.indexOf (qmString+"?")>=0) qmString += "?";
  // The largest sequence of question marks is qmString
  ArrayList<PImage> images = new ArrayList<PImage>();
  int ctr=0;
  do {
    String fname = filePattern.replace(qmString, nf(ctr, qmString.length()));
    InputStream input = createInput(fname);
    if (input==null) break;
    PImage img = loadImage (fname);
    if (img==null) break;
    images.add(img);
    ctr++;
  } while (true);
  return images;
}

// Images of the player animation for different phases
ArrayList<PImage> playerImgs;
// phase of the animation (see finite state machine in slides)
int playerPhase;
//////////////////////////////////////////////

//define the ArrayList of the enemys
ArrayList <Enemy> enemys=new ArrayList<Enemy>();

void setup() {
  smooth();
  size( 1000, 780 );
  backgroundImg = loadImage ("images/background.jpg"); // load the backgroundimage
  playerImgs=loadImages("images/player-??.png");


  //set size of the enemy arrays
  gX = new float[numOfEnemies];
  gY = new float[numOfEnemies];
  gVX = new float[numOfEnemies];

  //add enemies as long as the length of gX isn't reached
  for (int i = 0; i<gX.length; i++) {
    enemys.add(new Enemy(gX[i], gY[i]));
    //let the enemies walk in -2 steps on x-axis at the beginning
    gVX[i] = -2;
  }

  newGame ();
}

void newGame () {
  map = new Map( "demo.map");
  for ( int x = 0; x < map.w; ++x ) {
    for ( int y = 0; y < map.h; ++y ) {
      // put player at 'S' tile and replace with 'F'
      if ( map.at(x, y) == 'S' ) {
        playerX = map.centerXOfTile (x);
        playerY= map.centerYOfTile (y);
        map.set(x, y, 'F');
      }
      //put all enemies on the map on the tiles with a tilename on the array enemyStartTileName, replace those tiles with 'F'
      for (int i = 0; i<gX.length; i++) {
        if ( map.at(x, y) == enemyStartTileName[i] ) {
          gX[i] = map.centerXOfTile (x);
          gY[i] = map.centerYOfTile (y);
          map.set(x, y, 'F');
        }
        //search for first Clock tile and define clock Position
        if ( map.at(x, y) == 'A' ) {
          clockPosX= map.centerXOfTile (x);
          clockPosY= map.centerYOfTile (y);
        }
      }
    }
  }
  time=0;
  counter=0;
  clockDeg=0;
  playerVX = 0;
  playerVY = 0;
  gravity=0;
  gameState = GAMEWAIT;
}


// Maps x to an output y = map(x,xRef,yRef,factor), such that
//     - x0 is mapped to y0
//     - increasing x by 1 increases y by factor
float map (float x, float xRef, float yRef, float factor) {
  return factor*(x-xRef)+yRef;
}

void updateEnemy() {

  float [] nextgX;
  nextgX = new float[numOfEnemies];

  for (int i = 0; i<nextgX.length; i++) {

    nextgX[i] = gX[i]+ gVX[i];


    // do the following only when wall is between player and enemy (let the enemies walk up and down on x-axis, while not seeing the player)
    if (map.testTileOnLine (playerX, playerY, gX[i], gY[i], "W")) {

      //collision left-upper-corner of enemy with left side of walls
      if ( map.testTileInRect(nextgX[i]-gDiameter/2, gY[i]-14, gDiameter/2, gDiameter, "W" )) {
        gVX[i] = -gVX[i];
        nextgX[i] = gX[i];
      }

      //collision right-upper-corner of player with right side of walls
      if ( map.testTileInRect(nextgX[i], gY[i]-gDiameter/2, gDiameter/2, gDiameter, "W" )) {
        gVX[i] = -gVX[i];
        nextgX[i] = gX[i];
      }

      //debugging for modus after hunting player
      if ( map.testTileFullyInsideRect(nextgX[i]-gDiameter, gY[i]-gDiameter/2, gDiameter/2, gDiameter, "W" )) {
        gX[i] += 1;
        gVX[i] = -gVX[i];
        nextgX[i] = gX[i];
      }

      //debugging for modus after hunting player
      if ( map.testTileFullyInsideRect(nextgX[i]+gDiameter/2, gY[i], gDiameter, gDiameter, "W" )) {
        gX[i] -= 1;
        gVX[i] = -gVX[i];
        nextgX[i] = gX[i];
      }



      gX[i] = nextgX[i];
    }
  }
}


void drawBackground() {
  // Explanation to the computation of x and y:
  // If screenLeftX increases by 1, i.e. the main level moves 1 to the left on screen,
  // we want the background map to move 0.5 to the left, i.e. x decrease by 0.5
  // Further, imagine the center of the screen (width/2) corresponds to the center of the level
  // (map.widthPixel), i.e. screenLeftX=map.widthPixel()/2-width/2. Then we want
  // the center of the background image (backgroundImg.width/2) also correspond to the screen
  // center (width/2), i.e. x=-backgroundImg.width/2+width/2.
  float x = map (screenLeftX, map.widthPixel()/2-width/2, -backgroundImg.width/2+width/2, -0.5);
  float y = map (screenTopY, map.heightPixel()/2-height/2, -backgroundImg.height/2+height/2, -0.5);
  background(0);
//image (backgroundImg, x+1000, y+400);
}


void drawMap() {
  // The left border of the screen is at screenLeftX in map coordinates
  // so we draw the left border of the map at -screenLeftX in screen coordinates
  // Same for screenTopY.
  map.draw( -screenLeftX, -screenTopY );
}


void drawPlayer() {
  // draw player
  noStroke();
  //fill(0, 255, 255);
  imageMode(CENTER);
  image(playerImgs.get(playerPhase), playerX- screenLeftX, playerY - screenTopY); // depict the player
  fill(gC);

  // understanding this is optional, skip at first sight
  if (showSpecialFunctions) {
    // draw a line to the next hole
    Map.TileReference nextHole = map.findClosestTileInRect (playerX-100, playerY-100, 200, 200, "H");
    stroke(255, 0, 255);
    if (nextHole!=null) line (playerX-screenLeftX, playerY-screenTopY,
      nextHole.centerX-screenLeftX, nextHole.centerY-screenTopY);
  }
}

void drawText() {
  textAlign(CENTER, CENTER);
  fill(0, 255, 0);
  textSize(40);
  if (gameState==GAMEWAIT) text ("collect 3 flowers, press space to start", width/2, height/2);
  else if (gameState==GAMEOVER) text ("game over", width/2, height/2);
  else if (gameState==GAMEWON) text ("won in "+ round(time) + " seconds", width/2, height/2);
}


void draw() {
  if (gameState==GAMERUNNING) {
    updatePlayer();
    updateEnemy();
    movePlayer();
    //let the enemys move
    for (int i=0; i<enemys.size(); i++) {
      enemys.get(i).moveEnemy(gY[i], gY[i]);
    }
    time+=1/frameRate;
  } else if (keyPressed && key==' ') {
    if (gameState==GAMEWAIT) gameState=GAMERUNNING;
    else if (gameState==GAMEOVER || gameState==GAMEWON) newGame();
  }
  //horizontal scrolling
  screenLeftX = playerX - width/2;

  //use the following if you want no vertical scrolling:
  //screenTopY  = (map.heightPixel() - height)/2;

  //use the following if you want vertical scrolling:
  screenTopY = playerY - height/2;

 drawBackground();
  drawMap();
  drawPlayer();
  drawClock();
  drawText();


  //draw the enemies
  for (int i=0; i<enemys.size(); i++) {
    enemys.get(i).drawEnemy(gX[i], gY[i], gDiameter);
  }

  //win when 3 flowers collected
  if (counter==2) gameState=GAMEWON;
  //lose when collision with enemy


  //let the enemy hunt the player when no wall is between them
  for (int i = 0; i < enemys.size(); i++) {
    if (!map.testTileOnLine (playerX-playerW, playerY, gX[i]+gDiameter/2, gY[i], "W") || !map.testTileOnLine (playerX-playerW, playerY, gX[i]-gDiameter/2, gY[i], "W")) gX[i]=lerp(playerX, gX[i], huntingSpeed);
  }

  //  if (clockDeg<0 || clockDeg>360) gameState=GAMEOVER ;

  clockSecs=int(clockDeg)/6;
  //  println("time passed: "+clockSecs);
  println("playerPhase= "+playerPhase);
  println("frameRate= "+frameRate);
}
