package org.langrid.mlgridservices.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

public class WavRecorder {
	public WavRecorder(String prefix, int channels, int sampleSizeInBits, int sampleRate) throws IOException{
		this.channels = channels;
		this.sampleSizeInBits = sampleSizeInBits;
		this.sampleRate = sampleRate;
		this.rawFile = new File(prefix + ".raw");
		this.wavFile = new File(prefix + ".wav");
		this.rawOs = new BufferedOutputStream(new FileOutputStream(rawFile));
	}

	public void onRawData(byte[] data) throws IOException {
		rawOs.write(data);
	}

	public void onRecordingFinished() throws IOException {
		rawOs.close();
		try(LittleEndianDataInputStream dis = new LittleEndianDataInputStream(
					new BufferedInputStream(new FileInputStream(rawFile)));
				LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(
						new BufferedOutputStream(new FileOutputStream(wavFile)));
				Writer w = new OutputStreamWriter(dos, "UTF-8")){
			int dataSize = (int)rawFile.length();
			w.write("RIFF");  // RIFFヘッダ
			w.flush();
			dos.writeInt(32 + dataSize); // これ以降のファイルサイズ
			dos.flush();
			w.write("WAVEfmt ");
			w.flush();
			dos.writeInt(16); // fmtチャンクのバイト数
			dos.writeShort(1); // フォーマットID
			dos.writeShort(channels); // チャンネル数
			dos.writeInt(sampleRate); // サンプリングレート
			dos.writeInt(sampleRate * 2); // データ速度
			dos.writeShort(2); // ブロックサイズ
			dos.writeShort(sampleSizeInBits); // サンプルあたりのビット数
			dos.flush();
			w.write("data"); // dataチャンク
			w.flush();
			dos.writeInt(dataSize); // 波形データのバイト数
			while(dis.available() > 0) {
				dos.write(dis.read());
			}
			dos.flush();
		}
	}

	private int channels;
	private int sampleSizeInBits;
	private int sampleRate;
	private File rawFile;
	private File wavFile;
	private OutputStream rawOs;
}
