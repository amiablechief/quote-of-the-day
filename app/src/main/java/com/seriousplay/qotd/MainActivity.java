package com.seriousplay.qotd;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class MainActivity extends ActionBarActivity {

	private TextView mTextView;
    private String STATE_QOTD;
	private static String TEXT_VALUE = "";
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextView = (TextView) findViewById(R.id.qotdview);
		/*if(savedInstanceState != null) {
			String savedText = savedInstanceState.getString(STATE_QOTD);
			mTextView.setText(savedText);
		}*/

		//Adds scrolling to the TextView
		mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
	}

    //Code to save state on orientation change
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mTextView = (TextView) findViewById(R.id.qotdview);
        outState.putString(STATE_QOTD, mTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mTextView = (TextView) findViewById(R.id.qotdview);
        mTextView.setText(STATE_QOTD);
    }

    @Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	private AssetManager getApplicationAssets() {
		// open random quotes file
		AssetManager assetmanager = getAssets();
		return assetmanager;
	}

	private String getAssetPath(AssetManager assetmanager) {
		String[] dirs = null;
		String[] files = null;
		String path = null;
		
		try {
			dirs = assetmanager.list("");	//get list of files / dirs from the project 'assets' directory
			files = assetmanager.list(dirs[2]);	//Directories are listed in alphabetical order so fetch the 'txt' directory
			path = dirs[2].toString() + "/" + files[0].toString();	//construct the path (there is only 1 file in the dir)
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	// Get the path for the random quote file
	private InputStreamReader getQuoteReader() throws IOException {
		// open random quotes file
		AssetManager assets = getApplicationAssets();
		String path = null;
		path = getAssetPath(assets);
		InputStream inputStream = null;

		try {
			inputStream = assets.open(path);
			Log.v("QotD path", path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStreamReader textReader = new InputStreamReader(inputStream);
		return textReader;
	}

	// Get the total number of lines in the file
	private int getFileLineCount(InputStreamReader textReader) {
		BufferedReader br = new BufferedReader(textReader);
		int lineCount = 0;
		try {
			while ((br.readLine()) != null) {
				lineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lineCount; // total number of lines in the text file
	}

	// Return a random line number from where to get the 
	// corresponding quote string
	private int getRandomLineNumber(int totalLines) {
		Random rand = new Random();
		return rand.nextInt(totalLines);
	}

	private String getRandomQuote(int lineToFetch)
			throws IOException {
		//1. get path
		AssetManager assets = getApplicationAssets();
		String path = null;
		path = getAssetPath(assets);
		
		//2. open assets
		InputStream stream = assets.open(path);
		InputStreamReader randomQuote = new InputStreamReader(stream);
		
		//3. Get BufferedReader object
		BufferedReader buf = new BufferedReader(randomQuote);

		String quote = null; 
		String line = null;
		int currLine = 0;

		//4. Loop through using the new InputStreamReader until a match is found
		while ((line = buf.readLine()) != null && currLine < lineToFetch) {
			currLine++;
		}
		
		//Got the quote
		quote = line;

		//Clean up
		randomQuote.close();
		buf.close();

		return quote;
	}

	// Set the EditText widget to display the new random quote
	private void displayQuote(String quote) {
		TextView quoteDisplay = (TextView) findViewById(R.id.qotdview);
		TEXT_VALUE = quote;
		quoteDisplay.setText(TEXT_VALUE);
	}

	// onClick handler for the button click
	public void fetchQotD(View view) throws IOException {
		// open random quotes file
		InputStreamReader textReader = getQuoteReader();

		final int totalLines = getFileLineCount(textReader);
		int lineToFetch = 0;
		String quote = null;

		// We want to get the quote at the following line number
		lineToFetch = getRandomLineNumber(totalLines);
		
		quote = getRandomQuote(lineToFetch);

		displayQuote(quote);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem shareItem = menu.findItem(R.id.menu_item_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Handle item selection
		int id = item.getItemId();
		
		if(id == R.id.menu_item_share) {
			if(TEXT_VALUE == "") {
				Toast.makeText(this, "Nothing to share! First generate a quote by clicking the button", Toast.LENGTH_SHORT).show();
			} else {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, TEXT_VALUE);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Thought you might like this interesting Quote");
				startActivity(Intent.createChooser(shareIntent, "Share the quote via..."));
			}
		} else if (id == R.id.action_about) {
			//Toast.makeText(getApplicationContext(), "Please visit github.com/kulinp/QotD", Toast.LENGTH_LONG).show();
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
			//return true;
		} else if (id == R.id.action_settings) {
			Toast.makeText(getApplicationContext(), "Settings not yet implemented", Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}