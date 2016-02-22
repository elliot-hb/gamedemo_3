class Enemy {
  
  float enemyX;
  float enemyY;
  
  //constructor
  Enemy(float _enemyX, float _enemyY) {
    enemyX = _enemyX;
    enemyY = _enemyY;
  }

void drawEnemy(float _enemyX, float _enemyY, float _gDiameter) {
    ellipse(_enemyX-screenLeftX, _enemyY - screenTopY, _gDiameter, _gDiameter);
}

void moveEnemy(float _enemyX, float _enemyVX) {
   _enemyX+=_enemyVX;
}

}