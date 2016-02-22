import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class gamedemo extends PApplet {

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

float gravity=0.5f; // define gravity
float floorHeight;

float [] gX;  //position of enemy on x axis
float [] gVX; //enemy velocity on x axis
float [] gY; //position of enemy on y axis
char [] enemyStartTileName = {'X', 'Y', 'Z'}; //list of the names of the tiles where enemies appear, add more tilenames, to get more enemies, but also increase the numOfEnemies then!
int numOfEnemies = 3; //numberOfEnemies
int gDiameter=56; //enemy diamete
float huntingSpeed = 0.95f; //increasing this will let the enemy hunt the player faster
int gC;


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
public ArrayList<PImage> loadImages (String filePattern) {
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

public void setup() {
  
  
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

public void newGame () {
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
public float map (float x, float xRef, float yRef, float factor) {
  return factor*(x-xRef)+yRef;
}

public void updateEnemy() {

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


public void drawBackground() {
  // Explanation to the computation of x and y:
  // If screenLeftX increases by 1, i.e. the main level moves 1 to the left on screen,
  // we want the background map to move 0.5 to the left, i.e. x decrease by 0.5
  // Further, imagine the center of the screen (width/2) corresponds to the center of the level
  // (map.widthPixel), i.e. screenLeftX=map.widthPixel()/2-width/2. Then we want
  // the center of the background image (backgroundImg.width/2) also correspond to the screen
  // center (width/2), i.e. x=-backgroundImg.width/2+width/2.
  float x = map (screenLeftX, map.widthPixel()/2-width/2, -backgroundImg.width/2+width/2, -0.5f);
  float y = map (screenTopY, map.heightPixel()/2-height/2, -backgroundImg.height/2+height/2, -0.5f);
  background(0);
//image (backgroundImg, x+1000, y+400);
}


public void drawMap() {
  // The left border of the screen is at screenLeftX in map coordinates
  // so we draw the left border of the map at -screenLeftX in screen coordinates
  // Same for screenTopY.
  map.draw( -screenLeftX, -screenTopY );
}


public void drawPlayer() {
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

public void drawText() {
  textAlign(CENTER, CENTER);
  fill(0, 255, 0);
  textSize(40);
  if (gameState==GAMEWAIT) text ("collect 3 flowers, press space to start", width/2, height/2);
  else if (gameState==GAMEOVER) text ("game over", width/2, height/2);
  else if (gameState==GAMEWON) text ("won in "+ round(time) + " seconds", width/2, height/2);
}


public void draw() {
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

  clockSecs=PApplet.parseInt(clockDeg)/6;
  //  println("time passed: "+clockSecs);
  println("playerPhase= "+playerPhase);
  println("frameRate= "+frameRate);
}
class Map
{  
  int mode = CORNER;


  // Constructor: tmptileSize is the width/height of one tile in pixel
  Map( int tmptileSize ) {
    tileSize = tmptileSize;
    images = new PImage[26];
  }

  // Constructor: Loads a map file
  Map( String mapFile ) {
    images = new PImage[26];
    loadFile( mapFile );
  }

  //! Sets the mode in which coordinates are specified, supported is CORNER, CENTER, CORNERS
  public void mode (int tmpMode) { 
    mode=tmpMode;
  }


  public int widthPixel() {
    return w * tileSize;
  }

  public int heightPixel() {
    return h * tileSize;
  }

  // Left border (pixel) of the tile at tile position x
  public int leftOfTile(int x) {
    return x * tileSize;
  }

  // Right border (pixel) of the tile at tile position x
  public int rightOfTile(int x) {
    return (x+1) * tileSize-1;
  }

  // Top border (pixel) of the tile at tile position y
  public int topOfTile(int y) {
    return y * tileSize;
  }

  // Bottom border (pixel) of the tile at tile position y
  public int bottomOfTile(int y) {
    return (y+1) * tileSize-1;
  }

  //! Center of the tile at tile position x
  public int centerXOfTile (int x) {
    return x*tileSize+tileSize/2;
  }

  //! Center of the tile at tile position x
  public int centerYOfTile (int y) {
    return y*tileSize+tileSize/2;
  }
  
  //! Returns tile x-position of the tile at pixel x-position x
  public int xOfTileAtPixel (float x) {
    return floor(x/tileSize);
  }
  
  //! Returns tile y-position of the tile at pixel y-position x
  public int yOfTileAtPixel (float y) {
    return floor(y/tileSize);
  }
  
  //! Returns the left border of the tile at pixel position x
  public int leftOfTileAtPixel (float x) {
    return leftOfTile (xOfTileAtPixel(x));
  }

  //! Returns the right border of the tile at pixel position x
  public int rightOfTileAtPixel (float x) {
    return rightOfTile (xOfTileAtPixel(x));
  }
  
  //! Returns the x-center of the tile at pixel position x
  public int centerXOfTileAtPixel (float x) {
    return centerXOfTile (xOfTileAtPixel(x));
  }

  //! Returns the top border of the tile at pixel position y
  public int topOfTileAtPixel (float y) {
    return topOfTile (yOfTileAtPixel(y));
  }

  //! Returns the right border of the tile at pixel position x
  public int bottomOfTileAtPixel (float y) {
    return bottomOfTile (yOfTileAtPixel(y));
  }
  
  //! Returns the y-center of the tile at pixel position y
  public int centerYOfTileAtPixel (float y) {
    return centerYOfTile (yOfTileAtPixel(y));
  }


  // Returns the tile at tile position x,y. '_' for invalid positions (out of range)
  public char at( int x, int y ) {
    if ( x < 0 || y < 0 || x >= w || y >= h )
      return '_';
    else
      return map[y].charAt(x);
  }

  // Returns the tile at pixel position 'x,y', '_' for invalid
  public char atPixel (float x, float y) {
    return at (floor(x/tileSize), floor(y/tileSize));
  }

  // Sets the tile at tile position x,y
  // Coordinates below 0 are ignored, for coordinates
  // beyond the map border, the map is extended
  public void set (int x, int y, char ch) {
    if ( x < 0 || y < 0 ) return;
    extend (x+1, y+1);
    map[y] = replace (map[y], x, ch);
  }

  // Sets the tile at image position 'x,y' see set
  public void setPixel (int x, int y, char ch) {
    set (floor(x/tileSize), floor(y/tileSize), ch);
  }


  // Reference to a tile in the map  
  class TileReference {
    // Position in the map in tiles
    int x, y;
    // Position in the map in pixels
    // This position definitely belong to the tile (x,y)
    // where it is on the tile depents on the function returning this reference
    float xPixel, yPixel;
    // Type of the tile
    char tile;
    // Border of that tile in pixel
    int left, right, top, bottom;
    // Center of that tile in pixel
    int centerX, centerY;

    // Creates a reference to the tile at (x,y)
    // all other components are taken from the map
    TileReference (int tmpX, int tmpY) {
      x = tmpX;
      y = tmpY;
      setBorders();
      xPixel = centerX;
      yPixel = centerY;
    }

    // Computes tile, left, right, top, bottom, centerX, centerY from referenced tile
    public void setBorders() {
      tile = at(x, y);
      left = leftOfTile(x);
      right = rightOfTile(x);
      top = topOfTile(y);
      bottom = bottomOfTile(y);
      centerX =  centerXOfTile(x);
      centerY = centerYOfTile(y);
    }


    // Consider the line xPixel, yPixel towards goalX, goalY.
    // This line must start in tile x, y.
    // Then advanceTowards follows this line until it leaves x, y
    // updating xPixel,yPixel with the point where it leaves
    // and the rest with the tile it enters.
    public void advanceTowards (float goalX, float goalY)
    {
      float dX = goalX-xPixel;
      float dY = goalY-yPixel;
      // First try to go x until next tile
      float lambdaToNextX = Float.POSITIVE_INFINITY;
      if (dX>0) {
        float nextX = (x+1)*tileSize;
        lambdaToNextX = (nextX-xPixel)/dX;
      }   
      else if (dX<0) {
        float nextX = x*tileSize;
        lambdaToNextX = (nextX-xPixel)/dX;
      }
      // Then try to go y until next tile
      float lambdaToNextY = Float.POSITIVE_INFINITY;
      if (dY>0) {
        float nextY = (y+1)*tileSize;
        lambdaToNextY = (nextY-yPixel)/dY;
      }   
      else if (dY<0) {
        float nextY = y*tileSize;
        lambdaToNextY = (nextY-yPixel)/dY;
      }
      // Then choose which comes first x, y or goal
      if (lambdaToNextX<lambdaToNextY && lambdaToNextX<1) { // Go x
        xPixel += dX*lambdaToNextX;
        yPixel += dY*lambdaToNextX;
        if (dX>0) x++;
        else x--;
      }
      else if (lambdaToNextY<=lambdaToNextX && lambdaToNextY<1) { // Go y
        xPixel += dX*lambdaToNextY;
        yPixel += dY*lambdaToNextY;
        if (dY>0) y++;
        else y--;
      }
      else {// reached goal in same cell
        xPixel = goalX;
        yPixel = goalY;
      }
    }
  };
  
    // Returns a reference to a given pixel and its tile
    public TileReference newRefOfPixel (float pixelX, float pixelY) {
      TileReference ref = new TileReference (floor(pixelX/tileSize), floor(pixelY/tileSize));
      ref.xPixel = pixelX;
      ref.yPixel = pixelY;
      return ref;
    }


  // True if the rectangle given by x, y, w, h (partially) contains an element with a tile
  // from list. The meaning of x,y,w,h is governed by mode (CORNER, CENTER, CORNERS.
  public boolean testTileInRect( float x, float y, float w, float h, String list ) {
   if (mode==CENTER) {
     x-=w/2;
     y-=w/2;
   }
   if (mode==CORNERS) {
     w=w-x;
     h=h-y;
   }
   int startX = floor(x / tileSize), 
   startY = floor(y / tileSize), 
   endX   = floor((x+w) / tileSize), 
   endY   = floor((y+h) / tileSize);

   for ( int xx = startX; xx <= endX; ++xx )
   {
     for ( int yy = startY; yy <= endY; ++yy )
     {
       if ( list.indexOf( at(xx, yy) ) != -1 )
         return true;
     }
   }
   return false;
  }
  
  

  // Like testtileInRect(...) but returns a reference to the tile if one is found
  // and null else. The meaning of x,y,w,h is governed by mode (CORNER, CENTER, CORNERS.
  public TileReference findTileInRect( float x, float y, float w, float h, String list ) {
    if (mode==CENTER) {
      x-=w/2;
      y-=w/2;
    }
    if (mode==CORNERS) {
      w=w-x;
      h=h-y;
    }
    int startX = floor(x / tileSize), 
    startY = floor(y / tileSize), 
    endX   = floor((x+w) / tileSize), 
    endY   = floor((y+h) / tileSize);

    for ( int xx = startX; xx <= endX; ++xx )
    {
      for ( int yy = startY; yy <= endY; ++yy )
      {
        if ( list.indexOf( at(xx, yy) ) != -1 )
          return new TileReference(xx, yy);
      }
    }
    return null;
  }

  // Like findTileInRect(...) but returns a reference to the tile closest to the center
  public TileReference findClosestTileInRect( float x, float y, float w, float h, String list ) {
    if (mode==CENTER) {
      x-=w/2;
      y-=w/2;
    }
    if (mode==CORNERS) {
      w=w-x;
      h=h-y;
    }
    float centerX=x+w/2, centerY=y+h/2;
    int startX = floor(x / tileSize), 
    startY = floor(y / tileSize), 
    endX   = floor((x+w) / tileSize), 
    endY   = floor((y+h) / tileSize);

    int xFound=-1, yFound=-1;
    float dFound = Float.POSITIVE_INFINITY;
    for ( int xx = startX; xx <= endX; ++xx )
    {
      for ( int yy = startY; yy <= endY; ++yy )
      {
        if ( list.indexOf( at(xx, yy) ) != -1 ) {
          float d = dist(centerXOfTile(xx), centerYOfTile(yy), centerX, centerY);
          if (d<dFound) {
            dFound = d;
            xFound = xx;
            yFound = yy;
          }
        }
      }
    }
    if (dFound<Float.POSITIVE_INFINITY) return new TileReference (xFound, yFound);
    else return null;
  }

  // True if the rectangle is completely inside tiles from the list
  //The meaning of x,y,w,h is governed by mode (CORNER, CENTER, CORNERS.
  public boolean testTileFullyInsideRect( float x, float y, float w, float h, String list ) {
    if (mode==CENTER) {
      x-=w/2;
      y-=w/2;
    }
    if (mode==CORNERS) {
      w=w-x;
      h=h-y;
    }
    float centerX=x+w/2, centerY=y+h/2;
    int startX = floor(x / tileSize), 
    startY = floor(y / tileSize), 
    endX   = floor((x+w) / tileSize), 
    endY   = floor((y+h) / tileSize);

    for ( int xx = startX; xx <= endX; ++xx ) {
      for ( int yy = startY; yy <= endY; ++yy ) {
        if ( list.indexOf( at(xx, yy) ) == -1 ) return false;
      }
    }
    return true;
  }


  // Searches along the line from x1,y1 to x2,y2 for a tile from list
  // Returns the first found or null if none.
  public TileReference findTileOnLine( float x1, float y1, float x2, float y2, String list ) {
    TileReference ref = newRefOfPixel (x1, y1);
    int ctr=0;
    int maxCtr = floor(abs(x1-x2)+abs(y1-y2))/tileSize+3;
    while (ctr<=maxCtr && (ref.xPixel!=x2 || ref.yPixel!=y2)) {
      if (ctr>0) ref.advanceTowards (x2, y2);
      if (list.indexOf(at(ref.x, ref.y))!=-1) {
        ref.setBorders (); 
        return ref;
      }
      ctr++;
    }
    if (ctr>maxCtr) println ("Internal error in Map:findTileOnLine");
    return null;
  }

  // Returns, whether on the line from x1,y1 to x2,y2 there is a tile from list
  public boolean testTileOnLine ( float x1, float y1, float x2, float y2, String list ) {
    return findTileOnLine (x1, y1, x2, y2, list)!=null;
  }

  // Draws the map on the screen, where the origin, i.e. left/upper
  // corner of the map is drawn at \c leftX, topY regardless of mode
  public void draw( float leftX, float topY ) {
    pushStyle();
    imageMode(CORNER);
    int startX = floor(-leftX / tileSize), 
    startY = floor(-topY / tileSize);
    for ( int y = startY; y < startY + height/tileSize + 2; ++y ) {
      for ( int x  = startX; x < startX + width/tileSize + 2; ++x ) {
        PImage img = null;
        char tile = at( x, y );
        if ( tile == '_' )
          img = outsideImage;
        else if ('A'<=tile && tile<='Z')
          img = images[at( x, y ) - 'A'];
        if ( img != null )
          image( img, 
          x*tileSize + leftX, 
          y*tileSize + topY, 
          tileSize, tileSize );
      }
    }
    popStyle();
  } 

  // Loads a map file
  // element size is obtained from the first image loaded
  public void loadFile( String mapFile ) {
    map = loadStrings( mapFile );
    if (map==null) 
      throw new Error ("Map "+mapFile+" not found.");
    while (map.length>0 && map[map.length-1].equals (""))
      map = shorten(map);
    h = map.length;
    if ( h == 0 ) 
      throw new Error("Map has zero size");
    w = map[0].length();

    // Load images
    for (char c='A'; c<='Z'; c++) 
      images[c - 'A'] = loadImageRelativeToMap (mapFile, c + ".png" );        
    outsideImage = loadImageRelativeToMap (mapFile, "_.png");

    for ( int y = 0; y < h; ++y ) {
      String line = map[y];
      if ( line.length() != w )
        throw new Error("Not every line in map of same length");

      for ( int x = 0; x < line.length(); ++x ) {
        char c = line.charAt(x);
        if (c==' ' || c=='_') {
        }
        else if ('A'<=c && c<='Z') {
          if (images[c - 'A'] == null) 
            throw new Error ("Image for "+c+".png missing");
        }
        else throw new Error("map must only contain A-Z, space or _");
      }
    }    

    determinetileSize ();
  }

  // Saves the map into a file
  public void saveFile (String mapFile) {
    saveStrings (mapFile, map);
  }


  //********************************************************************************************
  //********* The code below this line is just for internal use of the library *****************
  //********************************************************************************************

  // Internal: load and Image and return null if not found
  protected PImage tryLoadImage (String imageFilename) {
    //println("Trying "+imageFilename);
    if (createInput(imageFilename)!=null) {
      //println("Found");
      return loadImage (imageFilename);
    }
    else return null;
  }

  // Internal: Loads an image named imageName from a locatation relative
  // to the map file mapFile. It must be either in the same
  // directory, or in a subdirectory images, or in a parallel
  // directory images.
  protected PImage loadImageRelativeToMap (String mapFile, String imageName) {
    File base = new File(mapFile);
    File parent = base.getParentFile();
    PImage img;
    img = tryLoadImage (new File (parent, imageName).getPath());
    if (img!=null) return img;
    img = tryLoadImage (new File (parent, "images/"+imageName).getPath());
    if (img!=null) return img;
    img = tryLoadImage (new File (parent, "../images/"+imageName).getPath());
    return img;
  }

  // Goes through all images loaded and determine stileSize as amx
  // If image sizes are not square and equal a warning message is printed
  protected void determinetileSize () {
    tileSize = 0;
    PImage[] allImages = (PImage[]) append (images, outsideImage);
    for (int i=0; i<allImages.length; i++) if (allImages[i]!=null) {
      if (tileSize>0 && 
        (allImages[i].width!=tileSize || allImages[i].height!=tileSize))
        println ("WARNING: Images are not square and of same size");
      if (allImages[i].width>tileSize)  tileSize = allImages[i].width;
      if (allImages[i].height>tileSize) tileSize = allImages[i].height;
    }
    if (tileSize==0) throw new Error ("No image could be loaded.");
  }

  // If the dimension of the map is below width times height
  // _ are appended in each line and full lines are appended
  // such that it is width times height.
  protected void extend (int width, int height) {
    while (height>h) {
      map = append(map, "");
      h++;
    }
    if (w<width) w = width;
    for (int y=0; y<h; y++) {
      while (map[y].length ()<w) 
        map[y] = map[y] + "_";
    }
  }

  // Replaces s.charAt(index) with ch
  public String replace (String s, int index, char ch) {
    return s.substring(0, index)+ch+s.substring(index+1, s.length());
  }


  // *** variables ***
  // tile x, y is map[y].charAt(x)
  String map[];
  // images[c-'A'] is the image for tile c
  PImage images[];
  // special image drawn outside the map
  PImage outsideImage;
  // map dimensions in tiles
  int w, h;
  // width and height of an element in pixels
  int tileSize;
}
public void drawClock() {
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
class Enemy {
  
  float enemyX;
  float enemyY;
  
  //constructor
  Enemy(float _enemyX, float _enemyY) {
    enemyX = _enemyX;
    enemyY = _enemyY;
  }

public void drawEnemy(float _enemyX, float _enemyY, float _gDiameter) {
    ellipse(_enemyX-screenLeftX, _enemyY - screenTopY, _gDiameter, _gDiameter);
}

public void moveEnemy(float _enemyX, float _enemyVX) {
   _enemyX+=_enemyVX;
}

}
public void movePlayer() {

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
      playerVX*=0.9f;
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

  playerVY+=gravity*1.3f;
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

public void keyReleased() {
  playeraX = 0;
}
public void updatePlayer() {
  // update player
  gravity=0.5f;
  float nextX = playerX + playerVX, 
    nextY = playerY + playerVY;

  //collision bottom-half of player with top of walls
  if ( map.testTileInRect(nextX-playerW, nextY, 2*playerW, playerW, "W" )) {
    playerVX = 0;
    playerVY = 0;
    nextX = playerX;
    nextY = playerY;
    gravity=0;
  }

  //debugging part if hanging with the butt in the wall
  if (keyPressed && keyCode == UP  && map.testTileInRect(nextX-playerW, nextY, 2*playerW, playerW, "W" )) {
    playerY= playerY-5;
    playerVX = 0;
    playerVY = 0;
    nextX = playerX;
    nextY = playerY;
    gravity=0;
  }

  //collision upper-half of player with bottom of walls
  if ( map.testTileInRect( nextX-playerW, nextY-playerW, 2*playerW, playerW, "W" )) {
    playerY = playerY+1;
    playerVX = -playerVX;
    playerVY = -playerVY;
    nextX = playerX;
    nextY = playerY;
    gravity=0;
  }

  //collision left-upper-corner of player with left side of walls
  if ( map.testTileInRect(nextX-playerW, nextY-playerW, playerW, playerW, "W" )) {
    playerX = playerX+10;
    playerVX = -playerVX;
    playerVY = -playerVY;
    nextX = playerX;
    nextY = playerY;
    gravity=0;
  }

  //collision right-upper-corner of player with right side of walls
  if ( map.testTileInRect(nextX, nextY-playerW, playerW, playerW, "W" )) {
    playerX = playerX-10;
    playerVX = -playerVX;
    playerVY = -playerVY;
    nextX = playerX;
    nextY = playerY;
    gravity=0;
  }

  //collect flowers
  Map.TileReference tile =map.findTileInRect(nextX-playerW, nextY-playerW, 2*playerW, 2*playerW, "P");
  if (tile!=null) {
    //levitationTimer=5;
    map.set(tile.x, tile.y, 'F');
    counter+=1;
  }

  playerX = nextX;
  playerY = nextY;
}
  public void settings() {  size( 1000, 780 );  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "gamedemo" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
