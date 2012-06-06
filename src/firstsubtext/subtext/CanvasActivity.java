package firstsubtext.subtext;

/***
 * This class draws a shape to the screen and allows the user 
 * to capture that shape
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import data.Shape;
import firstsubtext.subtext.R.id;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
//each activity is a state. 
//this is the photo capture activity. It takes a picture 


public class CanvasActivity extends Activity implements OnTouchListener {

	private GridView letter_grid;
	private LetterView letter_view;
	private boolean two_finger = false;
	private int savedNum = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Canvas Call", "Entered ON Create");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d("Canvas Call", "Set Content View");
		setContentView(R.layout.canvas);
		
		
		Log.d("Canvas Call", "Find Grid View");
		letter_grid = (GridView) findViewById(R.id.letter_grid);
		letter_grid.setAdapter(new LetterAdapter(this));

		letter_grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				addToCanvas(position);
			}
		});

		letter_view = new LetterView(this);
		FrameLayout canvas = (FrameLayout) findViewById(R.id.canvas_frame);
		canvas.addView(letter_view, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		letter_view.setOnTouchListener(this);
		
		Log.d("Canvas Call", "Ended Canvas");

		Button saveButton = (Button) findViewById(id.button_save_canvas);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveScreen();
			}
		});
		
		Button resetButton = (Button) findViewById(id.button_reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reset();
			}
		});
		
		
		final EditText et = (EditText) findViewById(R.id.font_name);
		et.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		    	EditText view = (EditText) v;
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
					String s = view.getText().toString();
					// File (or directory) to be moved
					File file = new File(Globals.getPath());

					// Destination directory
					File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),s);

					// Move file to new directory
					boolean success = file.renameTo(new File(dir, file.getName()));
					if (!success) {
						Globals.changeDirectory(dir);
						Log.d("Text", "Move Failed");
					}
		          return true;
		        }
		        return false;
		    }
		});
		
	}
	
	private void saveScreen(){
		//save the picture

		File pictureFile = Globals.getOutputMediaFile(Globals.MEDIA_TYPE_IMAGE, "MyCanvas_" +(savedNum++)+ Globals.timeStamp + ".png");
		
		if (pictureFile == null) {
			return;
		}


		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			Bitmap bmap = letter_view.getImageOut();
			bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			Log.d("Capture Activity", "File Created");
			

		} catch (FileNotFoundException e) {
			Log.d("", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("", "Error accessing file: " + e.getMessage());
		}
		
	}
	
	public void reset(){
		Intent intent = new Intent(this, LoadActivity.class);
		startActivity(intent);
	}

	public void addToCanvas(int id) {
		letter_view.addLetter(id);
		letter_view.invalidate();
	}

	// Implement the OnTouchListener callback
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("Touch", "Action: " + event.getAction());
		Log.d("Touch", "Action Index: " + event.getActionIndex());

		int selected;

		LetterView lv = (LetterView) v;


		if (event.getActionIndex() > 0) {
			two_finger = !two_finger;
			
			if(event.getActionIndex() > 2){
				LinearLayout ll = (LinearLayout) findViewById(id.canvas_fullscreen);
				Bitmap b = Bitmap.createBitmap(ll.getWidth(), ll.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b);
				ll.draw(c);
				
				File pictureFile = Globals.getOutputMediaFile(Globals.MEDIA_TYPE_IMAGE, "GRAB_"+ Globals.timeStamp+ "_"+ Integer.toString(Globals.grab_num++) + ".jpg");
				if (pictureFile == null) {
					return true;
				}

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					Bitmap out = Bitmap.createBitmap(b,0, 0, (int) ll.getWidth(), (int) ll.getHeight(), new Matrix(), false);

					out.compress(Bitmap.CompressFormat.JPEG, 60, fos);
					fos.close();
					Log.d("Capture Activity", "File Created");
					
					

				} catch (FileNotFoundException e) {
					Log.d("", "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d("", "Error accessing file: " + e.getMessage());
				}
				
				return false;
			}
			
			else if(event.getActionIndex() > 1){
				lv.removeCurrentLetter();
			}
			
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			selected = lv.locate((int) event.getX(), (int) event.getY());

			if (selected != lv.getCur() && lv.getCur() != -1)
				lv.deselect(lv.getCur());
			if (selected == -1)
				return true;
			lv.select(selected);

			// finger up - nothing selected
		} else if (event.getAction() == MotionEvent.ACTION_UP) {			
			
			selected = lv.getCur();
			if (selected != -1)
				lv.deselect(selected);

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			selected = lv.getCur();
			if (selected == -1)
				return true;

			lv.updatePosition(event.getX(), event.getY());

			if (two_finger) {
				try {

					lv.updateScale(Globals.getScale(event.getX(0),
							event.getY(0), event.getX(1), event.getY(1)));
					
//					lv.setRotations(Globals.getRotation(event.getX(0),
//							event.getY(0), event.getX(1), event.getY(1)));
					
					float[] center = Globals.getCenter(event.getX(0),
							event.getY(0), event.getX(1), event.getY(1));
					lv.updatePosition(center[0], center[1]);

				} catch (IllegalArgumentException e) {
					Log.d("Matrix", "Exception Scale: " + e.getMessage());
					two_finger = false;
				}


			}

		}
		lv.invalidate();
		return true;

	}

}
