// Generates src/main/resources/data/darkfantasy/structure/grand_archive.nbt
// A giant gothic archive/library: deepslate brick shell, buttresses, stained-glass
// rose windows, stair-vaulted roof, two levels of book galleries, columned nave,
// chandeliers, reading tables, and a loot dais at the far end.
//
// Run with: node tools/gen_grand_archive.mjs
import fs from 'node:fs';
import zlib from 'node:zlib';

const REF = 'src/main/resources/data/darkfantasy/structure/wizard_tower.nbt';
const OUT = 'src/main/resources/data/darkfantasy/structure/grand_archive.nbt';

// ---------- DataVersion: copy from an existing structure so it matches the MC version ----------
const ref = zlib.gunzipSync(fs.readFileSync(REF));
const needle = Buffer.concat([Buffer.from([3, 0, 11]), Buffer.from('DataVersion', 'ascii')]);
const di = ref.indexOf(needle);
if (di < 0) throw new Error('DataVersion not found in ' + REF);
const DATA_VERSION = ref.readInt32BE(di + needle.length);

// ---------- voxel grid ----------
const W = 39, H = 41, L = 75; // x, y, z
const grid = new Array(W * H * L).fill(null);
const gi = (x, y, z) => (y * L + z) * W + x;
function set(x, y, z, name, props = null, nbt = null) {
  if (x < 0 || x >= W || y < 0 || y >= H || z < 0 || z >= L)
    throw new Error(`out of bounds: ${x},${y},${z} (${name})`);
  grid[gi(x, y, z)] = { name: 'minecraft:' + name, props, nbt };
}
function box(x1, y1, z1, x2, y2, z2, name, props = null) {
  for (let y = y1; y <= y2; y++)
    for (let z = z1; z <= z2; z++)
      for (let x = x1; x <= x2; x++) set(x, y, z, name, props);
}

const BTZ = [4, 10, 16, 22, 28, 34, 40, 46, 52, 58, 64, 70]; // buttress z positions
const WINZ = BTZ.slice(0, -1).map(z => z + 3);               // window centers between buttresses
const TRUSS = [10, 16, 22, 28, 34, 40, 46, 52, 58, 64];      // roof trusses / columns / chandeliers
const TABLES = [13, 25, 37, 49, 61];                          // reading table clusters
const LOOT = 'minecraft:chests/stronghold_library';

// ---------- 1. floor (y=0) ----------
for (let z = 0; z < L; z++)
  for (let x = 0; x < W; x++) {
    const interior = x >= 2 && x <= 36 && z >= 2 && z <= 72;
    if (!interior) { set(x, 0, z, 'polished_deepslate'); continue; }
    if (x === 9 || x === 29) { set(x, 0, z, 'polished_deepslate'); continue; }            // column strips
    if (TRUSS.includes(z) && x >= 10 && x <= 28) { set(x, 0, z, 'polished_deepslate'); continue; } // nave cross-bands
    set(x, 0, z, 'dark_oak_planks');
  }

// ---------- 2. walls (x=1 / x=37 long walls, z=1 / z=73 end walls), cornice at y=20 ----------
box(1, 1, 2, 1, 19, 72, 'deepslate_bricks');
box(37, 1, 2, 37, 19, 72, 'deepslate_bricks');
box(1, 1, 1, 37, 19, 1, 'deepslate_bricks');
box(1, 1, 73, 37, 19, 73, 'deepslate_bricks');
box(1, 20, 2, 1, 20, 72, 'polished_deepslate');
box(37, 20, 2, 37, 20, 72, 'polished_deepslate');
box(1, 20, 1, 37, 20, 1, 'polished_deepslate');
box(1, 20, 73, 37, 20, 73, 'polished_deepslate');

// ---------- 3. side windows (in x=1 / x=37 walls) ----------
for (const wz of WINZ) {
  for (const x of [1, 37]) {
    // small ground-level windows
    box(x, 3, wz - 1, x, 5, wz + 1, 'purple_stained_glass');
    // tall arched upper windows with a magenta center column
    box(x, 11, wz - 1, x, 18, wz + 1, 'purple_stained_glass');
    box(x, 11, wz, x, 19, wz, 'magenta_stained_glass');
  }
}

// ---------- 4. rose windows on front/back walls ----------
function rose(z, cx, cy, rGlass2, rMagenta2, rFrame2) {
  const r = Math.ceil(Math.sqrt(rFrame2));
  for (let dy = -r; dy <= r; dy++)
    for (let dx = -r; dx <= r; dx++) {
      const d2 = dx * dx + dy * dy;
      if (d2 <= rMagenta2) set(cx + dx, cy + dy, z, 'magenta_stained_glass');
      else if (d2 <= rGlass2) set(cx + dx, cy + dy, z, 'purple_stained_glass');
      else if (d2 <= rFrame2) set(cx + dx, cy + dy, z, 'chiseled_deepslate');
    }
}
rose(1, 19, 13, 13, 2, 20);
rose(73, 19, 13, 13, 2, 20);

// ---------- 5. entrance portal (front wall z=1) + protruding frame at z=0 ----------
box(17, 1, 1, 21, 6, 1, 'air'); // carve the archway
for (let y = 1; y <= 7; y++)
  for (let x = 16; x <= 22; x++)
    if (x === 16 || x === 22 || y === 7) set(x, y, 0, 'polished_deepslate');
set(19, 7, 0, 'chiseled_deepslate'); // keystone
set(16, 8, 0, 'lantern', { hanging: 'false' });
set(22, 8, 0, 'lantern', { hanging: 'false' });

// ---------- 6. roof: outer stair shell + inner full-block seal, ridge cap ----------
for (let i = 0; i <= 18; i++) {
  const y = 21 + i;
  for (let z = 0; z < L; z++) {
    set(i, y, z, 'deepslate_tile_stairs', { facing: 'east', half: 'bottom' });
    set(38 - i, y, z, 'deepslate_tile_stairs', { facing: 'west', half: 'bottom' });
    if (i <= 17) {
      set(i + 1, y, z, 'deepslate_tiles');
      set(37 - i, y, z, 'deepslate_tiles');
    }
  }
}
for (let z = 0; z < L; z++) {
  set(19, 39, z, 'deepslate_tiles');
  set(19, 40, z, 'deepslate_tile_slab', { type: 'bottom' });
}

// ---------- 7. gable walls under the roof (front/back) + small gable roses ----------
for (let i = 0; i <= 17; i++) {
  const y = 21 + i;
  for (const z of [1, 73])
    for (let x = i + 1; x <= 37 - i; x++) set(x, y, z, 'deepslate_bricks');
}
rose(1, 19, 26, 6, 1, 10);
rose(73, 19, 26, 6, 1, 10);

// ---------- 8. buttresses ----------
for (const z of BTZ) {
  box(0, 1, z, 0, 12, z, 'deepslate_bricks');
  set(0, 13, z, 'polished_deepslate_stairs', { facing: 'east', half: 'bottom' });
  box(38, 1, z, 38, 12, z, 'deepslate_bricks');
  set(38, 13, z, 'polished_deepslate_stairs', { facing: 'west', half: 'bottom' });
}
for (const x of [7, 31]) { // front
  box(x, 1, 0, x, 12, 0, 'deepslate_bricks');
  set(x, 13, 0, 'polished_deepslate_stairs', { facing: 'south', half: 'bottom' });
}
for (const x of [7, 15, 23, 31]) { // back
  box(x, 1, 74, x, 12, 74, 'deepslate_bricks');
  set(x, 13, 74, 'polished_deepslate_stairs', { facing: 'north', half: 'bottom' });
}

// ---------- 9. nave columns with flares, roof trusses, chandeliers ----------
for (const z of TRUSS) {
  for (const cx of [9, 29]) {
    set(cx, 1, z, 'polished_deepslate');
    box(cx, 2, z, cx, 18, z, 'deepslate_bricks');
    set(cx, 9, z, 'chiseled_deepslate');
    set(cx, 19, z, 'polished_deepslate');
    // base flares
    set(cx - 1, 1, z, 'polished_deepslate_stairs', { facing: 'east', half: 'bottom' });
    set(cx + 1, 1, z, 'polished_deepslate_stairs', { facing: 'west', half: 'bottom' });
    set(cx, 1, z - 1, 'polished_deepslate_stairs', { facing: 'south', half: 'bottom' });
    set(cx, 1, z + 1, 'polished_deepslate_stairs', { facing: 'north', half: 'bottom' });
    // capital flares
    set(cx - 1, 19, z, 'polished_deepslate_stairs', { facing: 'east', half: 'top' });
    set(cx + 1, 19, z, 'polished_deepslate_stairs', { facing: 'west', half: 'top' });
    set(cx, 19, z - 1, 'polished_deepslate_stairs', { facing: 'south', half: 'top' });
    set(cx, 19, z + 1, 'polished_deepslate_stairs', { facing: 'north', half: 'top' });
  }
  // tie beam across the hall
  box(2, 20, z, 36, 20, z, 'dark_oak_log', { axis: 'x' });
  // central chandelier
  set(19, 19, z, 'chain', { axis: 'y' });
  set(19, 18, z, 'chain', { axis: 'y' });
  set(19, 17, z, 'lantern', { hanging: 'true' });
  set(15, 19, z, 'lantern', { hanging: 'true' });
  set(23, 19, z, 'lantern', { hanging: 'true' });
}

// ---------- 10. side galleries (floor y=8, walk level y=9) ----------
box(2, 8, 2, 8, 8, 72, 'dark_oak_planks');
box(30, 8, 2, 36, 8, 72, 'dark_oak_planks');
// stairwell openings
box(2, 8, 4, 3, 8, 11, 'air');
box(2, 8, 63, 3, 8, 70, 'air');
box(35, 8, 4, 36, 8, 11, 'air');
box(35, 8, 63, 36, 8, 70, 'air');
// railings along the nave edge
box(8, 9, 2, 8, 9, 72, 'dark_oak_fence', { north: 'true', south: 'true' });
box(30, 9, 2, 30, 9, 72, 'dark_oak_fence', { north: 'true', south: 'true' });
// lanterns perched on the railings
for (const z of TABLES) {
  set(8, 10, z, 'lantern', { hanging: 'false' });
  set(30, 10, z, 'lantern', { hanging: 'false' });
}
// soul lantern sconces under the gallery lip
for (const z of TRUSS) {
  set(8, 7, z, 'soul_lantern', { hanging: 'true' });
  set(30, 7, z, 'soul_lantern', { hanging: 'true' });
}

// ---------- 11. gallery staircases (with solid underfill) ----------
function stairRun(xs, zStart, dir) { // dir +1 = ascends toward +z (facing south), -1 toward -z (facing north)
  for (let k = 0; k <= 7; k++) {
    const z = zStart + dir * k, y = 1 + k;
    for (const x of xs) {
      if (y > 1) box(x, 1, z, x, y - 1, z, 'deepslate_bricks');
      set(x, y, z, 'dark_oak_stairs', { facing: dir > 0 ? 'south' : 'north', half: 'bottom' });
    }
  }
}
stairRun([2, 3], 11, -1);  // west front
stairRun([2, 3], 63, +1);  // west back
stairRun([35, 36], 11, -1); // east front
stairRun([35, 36], 63, +1); // east back

// ---------- 12. bookshelf stacks ----------
for (let z = 12; z <= 60; z += 4) {
  // ground floor stacks (3 tall, slab-capped)
  box(3, 1, z, 6, 3, z, 'bookshelf');
  box(3, 4, z, 6, 4, z, 'dark_oak_slab', { type: 'bottom' });
  box(32, 1, z, 35, 3, z, 'bookshelf');
  box(32, 4, z, 35, 4, z, 'dark_oak_slab', { type: 'bottom' });
  // gallery stacks (2 tall)
  box(3, 9, z, 5, 10, z, 'bookshelf');
  box(3, 11, z, 5, 11, z, 'dark_oak_slab', { type: 'bottom' });
  box(33, 9, z, 35, 10, z, 'bookshelf');
  box(33, 11, z, 35, 11, z, 'dark_oak_slab', { type: 'bottom' });
}

// ---------- 13. central carpet runner + reading tables ----------
box(18, 1, 2, 20, 1, 64, 'red_carpet');
for (const z of TABLES) {
  for (const [x1, x2] of [[12, 13], [25, 26]]) {
    box(x1, 1, z, x2, 1, z + 1, 'dark_oak_slab', { type: 'top' });
    set(x1 - 1, 1, z, 'dark_oak_stairs', { facing: 'west', half: 'bottom' });
    set(x1 - 1, 1, z + 1, 'dark_oak_stairs', { facing: 'west', half: 'bottom' });
    set(x2 + 1, 1, z, 'dark_oak_stairs', { facing: 'east', half: 'bottom' });
    set(x2 + 1, 1, z + 1, 'dark_oak_stairs', { facing: 'east', half: 'bottom' });
    set(x1, 2, z, 'candle', { candles: '2', lit: 'true' });
  }
}

// ---------- 14. dais at the far end ----------
box(13, 1, 66, 25, 1, 71, 'polished_deepslate');
box(13, 1, 65, 25, 1, 65, 'polished_deepslate_stairs', { facing: 'south', half: 'bottom' });
for (let z = 66; z <= 70; z++)
  for (let x = 15; x <= 23; x++)
    if (x === 15 || x === 23 || z === 70) {
      box(x, 2, z, x, 3, z, 'bookshelf');
      set(x, 4, z, 'dark_oak_slab', { type: 'bottom' });
    }
set(19, 2, 68, 'enchanting_table');
set(17, 2, 68, 'lectern', { facing: 'north' });
set(21, 2, 68, 'lectern', { facing: 'north' });
set(16, 2, 69, 'chest', { facing: 'north' }, { id: 'minecraft:chest', LootTable: LOOT });
set(22, 2, 69, 'chest', { facing: 'north' }, { id: 'minecraft:chest', LootTable: LOOT });
set(16, 2, 66, 'soul_lantern', { hanging: 'false' });
set(22, 2, 66, 'soul_lantern', { hanging: 'false' });

// ---------- 15. gallery loot chests ----------
set(3, 9, 72, 'chest', { facing: 'north' }, { id: 'minecraft:chest', LootTable: LOOT });
set(35, 9, 72, 'chest', { facing: 'north' }, { id: 'minecraft:chest', LootTable: LOOT });

// ---------- export as structure-template NBT ----------
const palette = [];
const pmap = new Map();
function pid(name, props) {
  const k = name + '|' + (props ? JSON.stringify(Object.keys(props).sort().map(p => [p, props[p]])) : '');
  let i = pmap.get(k);
  if (i === undefined) { i = palette.length; palette.push({ name, props }); pmap.set(k, i); }
  return i;
}
const AIR = pid('minecraft:air', null);
const blocks = [];
let nonAir = 0;
for (let y = 0; y < H; y++)
  for (let z = 0; z < L; z++)
    for (let x = 0; x < W; x++) {
      const b = grid[gi(x, y, z)];
      if (!b || b.name === 'minecraft:air') { blocks.push({ x, y, z, s: AIR }); continue; }
      nonAir++;
      blocks.push({ x, y, z, s: pid(b.name, b.props), nbt: b.nbt });
    }

const chunks = [];
const u8 = v => chunks.push(Buffer.from([v]));
const i16 = v => { const b = Buffer.alloc(2); b.writeInt16BE(v); chunks.push(b); };
const i32 = v => { const b = Buffer.alloc(4); b.writeInt32BE(v); chunks.push(b); };
const wstr = s => { const b = Buffer.from(s, 'utf8'); i16(b.length); chunks.push(b); };
const tag = (t, n) => { u8(t); wstr(n); };

u8(10); wstr(''); // unnamed root compound
tag(9, 'size'); u8(3); i32(3); i32(W); i32(H); i32(L);
tag(9, 'entities'); u8(0); i32(0);
tag(9, 'blocks'); u8(10); i32(blocks.length);
for (const b of blocks) {
  tag(9, 'pos'); u8(3); i32(3); i32(b.x); i32(b.y); i32(b.z);
  tag(3, 'state'); i32(b.s);
  if (b.nbt) {
    tag(10, 'nbt');
    for (const [k, v] of Object.entries(b.nbt)) { tag(8, k); wstr(v); }
    u8(0);
  }
  u8(0);
}
tag(9, 'palette'); u8(10); i32(palette.length);
for (const p of palette) {
  tag(8, 'Name'); wstr(p.name);
  if (p.props) {
    tag(10, 'Properties');
    for (const k of Object.keys(p.props).sort()) { tag(8, k); wstr(p.props[k]); }
    u8(0);
  }
  u8(0);
}
tag(3, 'DataVersion'); i32(DATA_VERSION);
u8(0); // end of root

const raw = Buffer.concat(chunks);
fs.writeFileSync(OUT, zlib.gzipSync(raw, { level: 9 }));

// ---------- verify: full re-parse of what we just wrote ----------
const back = zlib.gunzipSync(fs.readFileSync(OUT));
{
  let o = 0;
  const rb = () => back[o++];
  const rstr = () => { const n = back.readUInt16BE(o); o += 2; const s = back.toString('utf8', o, o + n); o += n; return s; };
  function payload(t) {
    switch (t) {
      case 1: o += 1; return;
      case 2: o += 2; return;
      case 3: o += 4; return;
      case 4: o += 8; return;
      case 5: o += 4; return;
      case 6: o += 8; return;
      case 7: { const n = back.readInt32BE(o); o += 4 + n; return; }
      case 8: { const n = back.readUInt16BE(o); o += 2 + n; return; }
      case 9: { const et = rb(); const n = back.readInt32BE(o); o += 4; for (let i = 0; i < n; i++) payload(et); return; }
      case 10: { for (;;) { const ct = rb(); if (ct === 0) return; rstr(); payload(ct); } }
      case 11: { const n = back.readInt32BE(o); o += 4 + 4 * n; return; }
      case 12: { const n = back.readInt32BE(o); o += 4 + 8 * n; return; }
      default: throw new Error(`bad tag ${t} at offset ${o}`);
    }
  }
  const t = rb(); rstr(); payload(t);
  if (o !== back.length) throw new Error(`verify failed: consumed ${o} of ${back.length} bytes`);
}

console.log('wrote', OUT);
console.log('DataVersion:', DATA_VERSION);
console.log(`size: ${W} x ${H} x ${L} (${blocks.length} block entries, ${nonAir} non-air)`);
console.log('palette entries:', palette.length);
console.log('palette:', palette.map(p => p.name.replace('minecraft:', '') + (p.props ? '[' + Object.entries(p.props).map(([k, v]) => k + '=' + v).join(',') + ']' : '')).join(', '));
console.log('file size:', (fs.statSync(OUT).size / 1024).toFixed(1), 'KiB (raw NBT', (raw.length / 1048576).toFixed(2), 'MiB)');
