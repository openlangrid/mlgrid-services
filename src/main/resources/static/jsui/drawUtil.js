drawPose = (()=>{
    valid = p=>{
        return p.x != 0 && p.y != 0;
    };
    line = (ctx, color, points, scale)=>{
        ctx.save();
        ctx.strokeStyle = color;
        ctx.lineWidth = 4;
        ctx.beginPath();
        let first = true;
        for(let i = 0; i < points.length; i++){
            if(!valid(points[i])) continue;
            const x = points[i].x * scale;
            const y = points[i].y * scale;
            if(first) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
            first = false;
        }
        ctx.stroke();
        ctx.restore();
    };
    circle = (ctx, color, points, scale)=>{
        ctx.save();
        ctx.fillStyle = color;
        ctx.beginPath();
        for(let i = 0; i < points.length; i++){
            if(!valid(points[i])) continue;
            const x = points[i].x * scale;
            const y = points[i].y * scale;
            ctx.arc(x, y, 3 * points[i].z, 0, Math.PI * 2);
        }
        ctx.fill();
        ctx.restore();
    };
    colors = [
        [255,   0,  85], [255,   0,   0], [255,  85,   0], [255, 170,   0], [255, 255,   0],
        [170, 255,   0], [ 85, 255,   0], [  0, 255,   0], [255,   0,   0], [  0, 255,  85],
        [  0, 255, 170], [  0, 255, 255], [  0, 170, 255], [  0,  85, 255], [  0,   0, 255],
        [255,   0, 170], [170,   0, 255], [255,   0, 255], [ 85,   0, 255], [  0,   0, 255],
        [  0,   0, 255], [  0,   0, 255], [  0, 255, 255], [  0, 255, 255], [  0, 255, 255]];
    names = [
        "Nose", "Neck",
        "RShoulder", "RElbow", "RWrist",
        "LShoulder", "LElbow", "LWrist",
        "MHip",
        "RHip", "RKnee", "RAnkle",
        "LHip", "LKnee", "LAnkle",
        "REye", "LEye", "REar", "LEar",
        "LBigToe", "LSmallToe", "LHeel",
        "RBigToe", "RSmallToe", "RHeel"];
    drawPairs = [
        [1,8], [1,2], [1,5], [2,3], [3,4],
        [5,6], [6,7], [8,9], [9,10], [10,11],
        [8,12], [12,13], [13,14], [1,0], [0,15],
        [15,17], [0,16], [16,18], [14,19], [19,20],
        [14,21], [11,22], [22,23], [11,24]
    ];
    return (ctx, scale, pose)=>{
        for(const dp of drawPairs){
            const i1 = dp[0];
            const i2 = dp[1];
            const p1 = pose[names[i1]];
            const p2 = pose[names[i2]];
            const c = colors[i2];
            line(ctx, `rgba(${c[0]}, ${c[1]}, ${c[2]}, 0.6)`, [p1, p2], scale);
        }
        for(let i = 0; i < names.length; i++){
            const p = pose[names[i]];
            const c = colors[i];
            circle(ctx, `rgba(${c[0]}, ${c[1]}, ${c[2]}, 0.7)`, [p], scale);
        }
    };
})();
