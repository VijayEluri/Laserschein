/**
 *  
 *  Laserschein. interactive ILDA output from processing and java
 *
 *  2012 by Benjamin Maus
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Benjamin Maus (http://www.allesblinkt.com)
 *
 */
package laserschein;

import java.util.Vector;

import processing.core.PApplet;

import com.sun.jna.ptr.IntByReference;


/**
 * Is going to provide a connection to the Easylase USB 2 ILDA box
 * 
 * @author Benjamin Maus
 */
public class EasylaseUsb2Adaptor extends AbstractLaserOutput {
	
	private static final int MAX_POINTS = 16000;
	private static final int MIN_SPEED = 1000;
	private static final int MAX_SPEED = 65535;
	
	private static final int COORDINATE_RANGE = 4095;

	private int _myCardNumber = 0;
	
	
	private EasylaseUsb2Native.Point.ByReference _myFirstPoint;
	private EasylaseUsb2Native.Point.ByReference[] _myPoints;
	
	
	public EasylaseUsb2Adaptor() {
		super();
	}
	

	@Override
	public void initialize() {
	
		if (EasylaseUsb2Native.state == NativeState.READY) {
			Logger.printInfo("Initializing");
			_myInitialize();
		}
		
		if (EasylaseUsb2Native.state == NativeState.UNSUPPORTED_PLATFORM) {
			Logger.printError("Platform not supported. Only Windows >XP works for now.");
			_myState = OutputState.UNSUPPORTED_PLATFORM;
		}
		
		if (EasylaseUsb2Native.state == NativeState.NOT_FOUND) {
			Logger.printError("The jmlaser.dll could not be loaded.");
			_myState = OutputState.LIBRARY_ERROR;
		}
		
	}
	
	

	@Override
	public void draw(final LaserFrame theFrame) {

		final Vector<LaserPoint> myPoints = theFrame.points();

		/* Point count */
		final int myNumberOfPoints = myPoints.size();

		if (myNumberOfPoints > getMaximumNumberOfPoints()) {
			Logger.printWarning( "Too many points to draw. Maximum number is " + MAX_POINTS);
		}

		final int myDisplayedNumberOfPoints = Math.min(myNumberOfPoints, getMaximumNumberOfPoints());


		/* Points */
		for (int i = 0; i < myDisplayedNumberOfPoints; i++) {
			final LaserPoint myPoint = myPoints.get(i);

			int myX = PApplet.constrain(_myTransformX(myPoint.x), 0, COORDINATE_RANGE);
			int myY = PApplet.constrain(_myTransformY(myPoint.y), 0, COORDINATE_RANGE);

			
			_myPoints[i].x = (short)myX;
			_myPoints[i].y = (short)myY;


			if (myPoint.isBlanked) {
				_myPoints[i].r = 0;
				_myPoints[i].g = 0;
				_myPoints[i].b = 0;
				_myPoints[i].i = 0;

			} else {
				_myPoints[i].r =  (byte)PApplet.constrain(myPoint.r, 0, 255);
				_myPoints[i].g =  (byte)PApplet.constrain(myPoint.g, 0, 255);
				_myPoints[i].b =  (byte)PApplet.constrain(myPoint.b, 0, 255);
				_myPoints[i].i =  (byte)PApplet.constrain(myPoint.r + myPoint.g + myPoint.b, 0, 255);
			}


			/* Manually update native memory */
			_myPoints[i].writeField("x");
			_myPoints[i].writeField("y");
			_myPoints[i].writeField("r");
			_myPoints[i].writeField("g");
			_myPoints[i].writeField("b");
			_myPoints[i].writeField("i");
			


		}

		/* Send and update */
		/* Handshake */
		if(EasylaseUsb2Native.EasyLaseGetStatus(new IntByReference(_myCardNumber)) == 1) {
			EasylaseUsb2Native.EasyLaseWriteFrame(new IntByReference(_myCardNumber), _myPoints[0], myDisplayedNumberOfPoints * 8, (short)_myScanSpeed);
		} else {
			
		}
	}

	

	
	@Override
	public void destroy() {
		EasylaseUsb2Native.EasyLaseStop(new IntByReference(_myCardNumber));

		EasylaseUsb2Native.EasyLaseClose();		
	}

	
	@Override
	public int getMaximumNumberOfPoints() {	
		return MAX_POINTS;
	}
	



	
	@Override
	public int getMinumumScanSpeed() {
		return MIN_SPEED;
	}

	
	@Override
	public int getMaximumScanSpeed() {
		return MAX_SPEED;
	}
	
	
	private int _myTransformX(float theX) {
		return (int) PApplet.map(theX, -1, 1, 0, COORDINATE_RANGE);
	}
	
	
	private int _myTransformY(float  theY) {
		return (int) PApplet.map(theY, -1, 1, 0,  COORDINATE_RANGE);
	}
	
	
	
	private void _myInitialize() {
	
		final int myNumberOfCards = EasylaseUsb2Native.EasyLaseGetCardNum();
		
		Logger.printInfo("Number of installed cards: " + myNumberOfCards);

		if (myNumberOfCards < 1) {
			Logger.printError("No cards connected found. ");

			_myState = OutputState.NO_DEVICES_FOUND;

		} else {
			Logger.printInfo("Initialization was successful");

			_myAllocateMemory();

			_myState = OutputState.READY;
		}

	}
	
	
	private void _myAllocateMemory() {

		/* Points */
		// Very uncanny way of allocating contiguous memory. Do not create
		// new Point Objects after that step. Use the allocated!
		_myFirstPoint = new EasylaseUsb2Native.Point.ByReference();
		_myPoints = (EasylaseUsb2Native.Point.ByReference[])_myFirstPoint.toArray(MAX_POINTS);

		for (int i = 0; i < MAX_POINTS; i++) {
			_myPoints[i].setAutoRead(false);  // we do not expect this to be changed by the DLL
			
			_myPoints[i].x = 0;
			_myPoints[i].y = 0;
		
			_myPoints[i].r = 0;
			_myPoints[i].g = 0;
			_myPoints[i].b = 0;

			_myPoints[i].i = 0;

			_myPoints[i].setAutoWrite(false); // we do not expect this to be changed by the DLL
		}
	}

}
