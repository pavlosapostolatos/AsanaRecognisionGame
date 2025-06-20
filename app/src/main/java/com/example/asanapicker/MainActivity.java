package com.example.asanapicker;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private List<String> imagePaths = new ArrayList<>();
    private Random random = new Random();
    private int currentRound = 0;
    private String currentAnswer;
    private boolean isNameTheImageMode;
    private ImageView mainImageView;
    private ImageView[] pickImageViews = new ImageView[5];
    private TextView instructionTextView;
    private TextView resultTextView;
    private EditText answerEditText;
    private Button submitButton;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadImages();
        startNewRound();
    }

    private void initializeViews() {
        instructionTextView = findViewById(R.id.instructionTextView);
        resultTextView = findViewById(R.id.resultTextView);
        mainImageView = findViewById(R.id.mainImageView);
        answerEditText = findViewById(R.id.answerEditText);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);

        pickImageViews[0] = findViewById(R.id.pickImageView1);
        pickImageViews[1] = findViewById(R.id.pickImageView2);
        pickImageViews[2] = findViewById(R.id.pickImageView3);
        pickImageViews[3] = findViewById(R.id.pickImageView4);
        pickImageViews[4] = findViewById(R.id.pickImageView5);

        for (int i = 0; i < 5; i++) {
        final int index = i;
        pickImageViews[i].setOnClickListener(v -> imageClicked(index));
    }

        submitButton.setOnClickListener(v -> submitAnswer());
        nextButton.setOnClickListener(v -> startNewRound());
    }

    private void loadImages() {
        AssetManager assetManager = getAssets();
        try {
            String[] files = assetManager.list("yoga_images");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".jpg") || file.endsWith(".png")) {
                        imagePaths.add("yoga_images/" + file);
                    }
                }
            }
            if (imagePaths.size() < 5) {
                Toast.makeText(this, "Please add at least 5 images to the assets/yoga_images folder.", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error loading yoga_images.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startNewRound() {
        resultTextView.setText("");
        answerEditText.setText("");
        answerEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        mainImageView.setVisibility(View.GONE);
        for (ImageView imageView : pickImageViews) {
        imageView.setVisibility(View.GONE);
        imageView.setEnabled(true);
        // Reset border
        imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.image_border));
    }

        isNameTheImageMode = currentRound % 2 == 0;
        currentRound++;

        if (isNameTheImageMode) {
            setupNameTheImageRound();
        } else {
            setupPickTheImageRound();
        }
    }

    private void setupNameTheImageRound() {
        instructionTextView.setText("Type the name of the image (filename without extension):");
        currentAnswer = imagePaths.get(random.nextInt(imagePaths.size()));
        try {
            InputStream inputStream = getAssets().open(currentAnswer);
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            mainImageView.setImageDrawable(drawable);
            inputStream.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
        }
        mainImageView.setVisibility(View.VISIBLE);
        answerEditText.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
        answerEditText.requestFocus();
    }

    private void setupPickTheImageRound() {
        instructionTextView.setText("Click the image that matches the name:");
        currentAnswer = imagePaths.get(random.nextInt(imagePaths.size()));
        String answerName = getFileNameWithoutExtension(currentAnswer);
        instructionTextView.setText("Select: " + answerName);

        List<String> selectedImages = new ArrayList<>();
        selectedImages.add(currentAnswer);
        while (selectedImages.size() < 5) {
            String img = imagePaths.get(random.nextInt(imagePaths.size()));
            if (!selectedImages.contains(img)) {
                selectedImages.add(img);
            }
        }
        Collections.shuffle(selectedImages);

        for (int i = 0; i < 5; i++) {
        try {
            InputStream inputStream = getAssets().open(selectedImages.get(i));
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            pickImageViews[i].setImageDrawable(drawable);
            inputStream.close();
            pickImageViews[i].setTag(selectedImages.get(i));
            pickImageViews[i].setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
        }
    }
    }

    private void submitAnswer() {
        String userAnswer = answerEditText.getText().toString().trim().toLowerCase();
        String correctName = getFileNameWithoutExtension(currentAnswer).toLowerCase();

        if (userAnswer.equals(correctName)) {
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            resultTextView.setText("Correct!");
        } else {
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            resultTextView.setText("Incorrect! The correct name is: " + correctName);
        }

        answerEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    private void imageClicked(int index) {
        String selectedImage = (String) pickImageViews[index].getTag();
        String correctName = getFileNameWithoutExtension(currentAnswer);

        if (selectedImage.equals(currentAnswer)) {
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            resultTextView.setText("Correct!");
        } else {
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            resultTextView.setText("Incorrect! The correct image is: " + correctName);
        }

        for (ImageView imageView : pickImageViews) {
        imageView.setEnabled(false);
        if (imageView.getTag().equals(currentAnswer)) {
            // Apply green border
            ShapeDrawable border = new ShapeDrawable(new RectShape());
            border.getPaint().setColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            border.getPaint().setStyle(Paint.Style.STROKE);
            border.getPaint().setStrokeWidth(8);
            Drawable[] layers = {ContextCompat.getDrawable(this, R.drawable.image_border), border};
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imageView.setBackground(layerDrawable);
        }
    }
    }

    private String getFileNameWithoutExtension(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}