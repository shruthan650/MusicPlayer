package com.shruthan.musicplayer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlay {

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

		File file = new File("src/naam.wav");
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
		Clip clip = AudioSystem.getClip();
		clip.open(audioInputStream);

		String response = "";

		while (!response.equals("Q")) {
			
			System.out.println("---Select a option:---");
			System.out.println("1. P-Play");
			System.out.println("2. R-Reset");
			System.out.println("3. S-Stop");
			System.out.println("4. Q-Quit");
			System.out.print("Enter an option: ");
			
			Scanner sc = new Scanner(System.in);

			response = sc.next();
			response = response.toUpperCase();

			switch (response) {

			case "P" -> clip.start();
			case "R" -> clip.setMicrosecondPosition(0);
			case "S" -> clip.stop();
			case "Q" -> clip.close();
			default -> System.out.println("Select a valid option");

			}
		}

		System.out.println("Byee!");
	}
}
