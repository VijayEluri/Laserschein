package laserschein.tests;

import processing.core.PApplet;
import laserschein.*;

@SuppressWarnings("serial")
public class TestAuxWindow extends PApplet {

	Laserschein laser;

	public void setup() {
		size(600, 600, OPENGL);
		frameRate(-1); // as fast as possible

		smooth();		
		laser = new Laserschein(this, Laserschein.EASYLASEUSB2);
		System.out.println(laser.output().getScanSpeed());

		laser.showControlWindow();
	
	}


	public void draw() {
		
		
		background(60);
			
		
		Laser3D renderer = laser.renderer();
		beginRaw(renderer);
				

		stroke(0,255,0);

		noFill(); // important!
		
		pushMatrix();
		translate(100,100);
		rotate(millis() * 0.0005f);
		drawBracket(20, (mouseY / (float) width) * 180f);
		popMatrix();

		
		pushMatrix();
		translate(200,100);
		rotate(millis() * 0.0005f);

		drawBracket(60, (mouseY / (float) width) * 180f);
		popMatrix();

		pushMatrix();
		translate(300,100);
		drawBracket(90, (mouseY / (float) width) * 180f);
		popMatrix();
	
		
		pushMatrix();
		translate(100,200);
		drawBracket(120, (mouseY / (float) width) * 180f);
		popMatrix();
		
		
		point(100,100);
		point(300,300);
		
		
		//beginShape();
		for(int i = 0; i < 400; i++){
			//vertex(random(0,width),random(0,height) );
		}
		//endShape();
		stroke(random(255),random(255),random(255));


		
		stroke(255,255,0);
		//ellipse(20,20, 400,400);
		
		endRaw();
		
		laser.drawDebugView();
		
		text(frameRate, 30, 100);

		
	}
	
	
	void drawBracket(float theAngle, float theLength) {
		float myY = sin(radians(theAngle) * 0.5f) * theLength;
		float myX = cos(radians(theAngle) * 0.5f) * theLength;

		beginShape();
		stroke(255,0,0);
		vertex(-myX, -myY);
		vertex(0, 0);
		vertex(myX, -myY);
		endShape();

		beginShape();
		stroke(0,255,0);

		vertex(myX, myY);
		vertex(0, 0);
		vertex(-myX, myY);
		endShape(CLOSE);
	}
	
	@Override
	public void keyPressed() {
		if(key == 'o') {
			laser.showControlWindow();
		}
		
		if(key == 'h') {
			laser.hideControlWindow();
		}
	}



	public static void main(String[] args) {
		
	

		PApplet.main(new String[] { TestAuxWindow.class.getCanonicalName() });
	}
}
