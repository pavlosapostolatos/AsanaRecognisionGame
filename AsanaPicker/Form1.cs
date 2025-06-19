using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Windows.Forms;

namespace AsanaPicker;

  public partial class Form1 : Form
    {
        private List<string> imagePaths;
        private Random random = new Random();
        private int currentRound = 0;
        private string currentAnswer;
        private bool isNameTheImageMode;
        private PictureBox[] imagePickBoxes = new PictureBox[5];
        private Label resultLabel;
        private TextBox answerBox;
        private Button submitButton;
        private Label instructionLabel;

        public Form1()
        {
            InitializeComponents();
            LoadImages();
            StartNewRound();
        }

        private void InitializeComponents()
        {
            this.Size = new Size(800, 600);
            this.Text = "Image Quiz Game";

            instructionLabel = new Label
            {
                Location = new Point(10, 10),
                Size = new Size(760, 30),
                TextAlign = ContentAlignment.MiddleCenter
            };
            this.Controls.Add(instructionLabel);

            // Initialize for "Pick the Image" mode
            for (int i = 0; i < 5; i++)
            {
                imagePickBoxes[i] = new PictureBox
                {
                    Location = new Point(10 + (i * 150), 100),
                    Size = new Size(140, 140),
                    SizeMode = PictureBoxSizeMode.StretchImage,
                    BorderStyle = BorderStyle.FixedSingle,
                    Visible = false
                };
                int index = i;
                imagePickBoxes[i].Click += (s, e) => ImageClicked(index);
                this.Controls.Add(imagePickBoxes[i]);
            }

            // Single picture box for "Name the Image" mode
            PictureBox mainImageBox = new PictureBox
            {
                Location = new Point(300, 100),
                Size = new Size(200, 200),
                SizeMode = PictureBoxSizeMode.StretchImage,
                BorderStyle = BorderStyle.FixedSingle,
                Visible = false,
                Name = "mainImageBox"
            };
            this.Controls.Add(mainImageBox);

            answerBox = new TextBox
            {
                Location = new Point(300, 320),
                Size = new Size(200, 30),
                Visible = false
            };
            this.Controls.Add(answerBox);

            submitButton = new Button
            {
                Location = new Point(300, 360),
                Size = new Size(200, 30),
                Text = "Submit",
                Visible = false
            };
            submitButton.Click += SubmitButton_Click;
            this.Controls.Add(submitButton);

            resultLabel = new Label
            {
                Location = new Point(10, 400),
                Size = new Size(760, 100),
                TextAlign = ContentAlignment.MiddleCenter,
                ForeColor = Color.Black
            };
            this.Controls.Add(resultLabel);

            Button nextButton = new Button
            {
                Location = new Point(300, 500),
                Size = new Size(200, 30),
                Text = "Next Round"
            };
            nextButton.Click += (s, e) => StartNewRound();
            this.Controls.Add(nextButton);
        }

        private void LoadImages()
        {
            string imageFolder = Path.Combine(Application.StartupPath, "Images");
            if (!Directory.Exists(imageFolder))
            {
                MessageBox.Show("Images folder not found! Please create an 'Images' folder with image files.");
                Close();
                return;
            }

            imagePaths = Directory.GetFiles(imageFolder, "*.jpg")
                .Concat(Directory.GetFiles(imageFolder, "*.png"))
                .ToList();

            if (imagePaths.Count < 5)
            {
                MessageBox.Show("Please add at least 5 images to the Images folder.");
                Close();
            }
        }

        private void StartNewRound()
        {
            resultLabel.Text = "";
            answerBox.Text = "";
            answerBox.Visible = false;
            submitButton.Visible = false;
            foreach (var box in imagePickBoxes)
                box.Visible = false;
            Controls.Find("mainImageBox", true)[0].Visible = false;

            isNameTheImageMode = currentRound % 2 == 0;
            currentRound++;

            if (isNameTheImageMode)
                SetupNameTheImageRound();
            else
                SetupPickTheImageRound();
        }

        private void SetupNameTheImageRound()
        {
            instructionLabel.Text = "Type the name of the image (filename without extension):";
            currentAnswer = imagePaths[random.Next(imagePaths.Count)];
            PictureBox mainImageBox = (PictureBox)Controls.Find("mainImageBox", true)[0];
            mainImageBox.Image = Image.FromFile(currentAnswer);
            mainImageBox.Visible = true;
            answerBox.Visible = true;
            submitButton.Visible = true;
            answerBox.Focus();
        }

        private void SetupPickTheImageRound()
        {
            instructionLabel.Text = "Click the image that matches the name:";
            currentAnswer = imagePaths[random.Next(imagePaths.Count)];
            string answerName = Path.GetFileNameWithoutExtension(currentAnswer);
            resultLabel.Text = $"Select: {answerName}";

            var selectedImages = new List<string> { currentAnswer };
            while (selectedImages.Count < 5)
            {
                string img = imagePaths[random.Next(imagePaths.Count)];
                if (!selectedImages.Contains(img))
                    selectedImages.Add(img);
            }
            selectedImages = selectedImages.OrderBy(x => random.Next()).ToList();

            for (int i = 0; i < 5; i++)
            {
                imagePickBoxes[i].Image = Image.FromFile(selectedImages[i]);
                imagePickBoxes[i].Visible = true;
                imagePickBoxes[i].Tag = selectedImages[i];
            }
        }

        private void SubmitButton_Click(object sender, EventArgs e)
        {
            string userAnswer = answerBox.Text.Trim().ToLower();
            string correctName = Path.GetFileNameWithoutExtension(currentAnswer).ToLower();

            if (userAnswer == correctName)
            {
                resultLabel.ForeColor = Color.Green;
                resultLabel.Text = "Correct!";
            }
            else
            {
                resultLabel.ForeColor = Color.Red;
                resultLabel.Text = $"Incorrect! The correct name is: {correctName}";
            }

            answerBox.Visible = false;
            submitButton.Visible = false;
        }

        private void ImageClicked(int index)
        {
            string selectedImage = (string)imagePickBoxes[index].Tag;
            string correctName = Path.GetFileNameWithoutExtension(currentAnswer);

            if (selectedImage == currentAnswer)
            {
                resultLabel.ForeColor = Color.Green;
                resultLabel.Text = "Correct!";
            }
            else
            {
                resultLabel.ForeColor = Color.Red;
                resultLabel.Text = $"Incorrect! The correct image is: {correctName}";
                foreach (var box in imagePickBoxes)
                    if ((string)box.Tag == currentAnswer)
                        box.BorderStyle = BorderStyle.Fixed3D;
            }

            foreach (var box in imagePickBoxes)
                box.Enabled = false;
        }
    }
