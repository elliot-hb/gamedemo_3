void updatePlayer() {
  // update player, actually the collisions are calculated here.
  gravity=0.5;
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
