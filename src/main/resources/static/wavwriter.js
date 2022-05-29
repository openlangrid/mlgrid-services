class WavWriter{
    /**
     * @param {Object} config {channels: 1, sampleSizeInBits: 16, sampleRate: 16000}
     */
    constructor(config){
        this.config = {channels: 1, sampleSizeInBits: 16, sampleRate: 16000};
        Object.assign(this.config, config);
        this.audioData = [];
    }

    /**
     * @param {ArrayBuffer} data 
     */
    addData(data){
        this.audioData.push(data);
    };

    /**
     * @return {ArrayBuffer}
     */
    getWavFile(){
        const dataSize = this.audioData.map(d=>d.byteLength).reduce((l, r)=>l+r);
        const buffer = new ArrayBuffer(44 + dataSize);
        var view = new DataView(buffer);

        const writeString = function(view, offset, string) {
            for (let i = 0; i < string.length; i++){
                view.setUint8(offset + i, string.charCodeAt(i));
            }
        };
        writeString(view, 0, 'RIFF');  // RIFFヘッダ
        view.setUint32(4, 32 + dataSize, true); // これ以降のファイルサイズ
        writeString(view, 8, 'WAVEfmt '); // WAVEヘッダ
        view.setUint32(16, 16, true); // fmtチャンクのバイト数
        view.setUint16(20, 1, true); // フォーマットID
        view.setUint16(22, this.config.channels, true); // チャンネル数
        view.setUint32(24, this.config.sampleRate, true); // サンプリングレート
        view.setUint32(28, this.config.sampleRate * 2, true); // データ速度
        view.setUint16(32, 2, true); // ブロックサイズ
        view.setUint16(34, this.config.sampleSizeInBits, true); // サンプルあたりのビット数
        writeString(view, 36, 'data'); // dataチャンク
        view.setUint32(40, dataSize, true); // 波形データのバイト数
        const dst = new Uint8Array(buffer, 44);
        let index = 0;
        for(let s of this.audioData){
            // Uint8Array同士にしてmemcpy
            dst.set(new Uint8Array(s), index);
            index += s.byteLength;
        }
        console.debug(`${buffer.byteLength} bytes file created.`);
        return buffer;
    };
}
