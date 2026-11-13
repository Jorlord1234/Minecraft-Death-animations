// Source of truth for the keyframes (same data as the browser preview).
// Run: node tools/export_clips.js   -> rewrites src/main/resources/assets/deathanimations/clips/*.json
const fs = require('fs');
const path = require('path');
const A = {
fall:{dur:2.6,kf:[
[0,{y:-230,armF:-170,armB:-150,legF:-8,legB:8}],
[0.55,{y:0},'in'],
[0.65,{armF:-40,armB:-30,legF:0,legB:0,eyes:1}],
[1.3,{rot:90,y:-10,armF:-170,armB:-160},'in'],
[1.5,{rot:84,y:-20,eyes:2}],
[1.8,{rot:90,y:-10,armF:15,armB:5}],
[2.6,{}]]},
stalagmite:{dur:5.2,kf:[
[0,{y:-240,armF:-165,armB:-150}],
[0.5,{y:-26,legF:-22,legB:22},'in'],
[0.55,{eyes:1,flash:0.9,armF:-70,armB:-50}],
[0.9,{flash:0}],
[1.8,{tint:0.3}],
[2.4,{tint:0.9}],
[2.9,{y:-36,legF:-3,legB:3,armF:-90,armB:-90,tint:1}],
[3.5,{}],
[4.1,{x:28,y:-10,rot:90,armF:-165,armB:-165,legF:0,legB:0,stars:1},'in'],
[4.3,{rot:83,y:-22,eyes:2}],
[4.6,{rot:90,y:-10}],
[5.2,{}]]},
anvil:{dur:3,kf:[
[0,{oy:-430}],
[0.6,{oy:-120},'in'],
[0.68,{oy:-15,sy:0.12,sx:1.5,eyes:1}],
[1.5,{eyes:2}],
[3,{}]]},
cactus:{dur:4,kf:[
[0,{x:-170,walk:1,armF:-12,armB:12}],
[1.1,{x:-8}],
[1.25,{walk:0,armF:-90,armB:-82,legF:0,legB:0}],
[1.8,{eyes:1,tint:0.5,spikes:1}],
[2.05,{x:-60,y:-34,armF:-125,armB:-105,tint:0.8}],
[2.3,{x:-76,y:0}],
[3.4,{x:-82,rot:-90,y:-10,armF:30,armB:10,eyes:2},'in'],
[4,{}]]},
fire:{dur:4.2,kf:[
[0,{armF:-15,armB:15}],
[0.4,{fire:1,hop:1,eyes:1,armF:-150,armB:-165}],
[2.2,{tint:0.6}],
[2.9,{hop:0,tint:0.9,armF:-160,armB:-160}],
[3.4,{sy:0.28,sx:1.35,ash:1,fire:0.35,eyes:3,armF:-30,armB:-20}],
[4.2,{}]]},
drown:{dur:5.2,kf:[
[0,{y:-250,armF:-10}],
[0.3,{y:-240,eyes:1,bub:1,flail:1}],
[2.8,{y:-6,flail:0.9}],
[3.2,{y:0,flail:0.2}],
[3.5,{flail:0}],
[4.9,{y:-246,rot:-90,eyes:2,bub:0.3,armF:-20,armB:20}],
[5.2,{}]]}
};
const out = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'deathanimations', 'clips');
fs.mkdirSync(out, { recursive: true });
for (const id in A) {
  const clip = { id, duration: A[id].dur, keys: A[id].kf.map(k => {
    const key = { t: k[0], set: k[1] };
    if (k[2] === 'in') key.ease = 'in';
    return key;
  }) };
  fs.writeFileSync(path.join(out, id + '.json'), JSON.stringify(clip, null, 1) + '\n');
  console.log('wrote', id + '.json');
}
module.exports = A;
