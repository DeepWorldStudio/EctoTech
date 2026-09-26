package ectotech.world.geometry;

import arc.func.Boolf;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.struct.IntSet;
import arc.struct.Seq;
import mindustry.gen.Building;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

/**
 * Tile-boundary geometry for expanding wave fronts.
 * Reads world geometry but never applies damage or creates bullets.
 */
public final class ArcWaveGeometry{
    public static final float angleEpsilon = 0.0001f;

    private ArcWaveGeometry(){}

    /** Unwrapped angular interval in degrees. */
    public static class Cut{
        public float start, end;

        public Cut(float start, float end){
            this.start = start;
            this.end = end;
        }

        public float length(){
            return end - start;
        }
    }

    /** A visible angular interval belonging to one surface owner. */
    public static class FaceHit extends Cut{
        public final Building build;
        public final int teamId;
        public final int axis;
        public final float plane;
        public final boolean positive;

        public FaceHit(
                Building build, int teamId,
                float start, float end,
                int axis, float plane, boolean positive
        ){
            super(start, end);
            this.build = build;
            this.teamId = teamId;
            this.axis = axis;
            this.plane = plane;
            this.positive = positive;
        }

        public long key(){
            return (Float.floatToIntBits(plane) & 0xffffffffL)
                    | ((long)axis << 32)
                    | (positive ? 1L << 33 : 0L)
                    | ((long)(teamId & 0xff) << 34);
        }
    }

    /** Local occupancy snapshot. Passed buildings can be excluded per segment. */
    public static class Scene{
        final int x, y, width, height;
        final Building[] cells;

        Scene(int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            cells = new Building[width * height];
        }

        Building get(int tx, int ty, IntSet ignored){
            tx -= x;
            ty -= y;

            if(tx < 0 || ty < 0 || tx >= width || ty >= height) return null;

            Building build = cells[tx + ty * width];
            return build != null && ignored.contains(build.id) ? null : build;
        }
    }

    /** Captures blocking tiles, including a one-tile neighbor margin. */
    public static Scene capture(
            float ox, float oy, float radius,
            Boolf<Building> blocks
    ){
        float half = tilesize / 2f;

        int minX = Math.max(0,
                Mathf.floor((ox - radius + half) / tilesize) - 1);
        int minY = Math.max(0,
                Mathf.floor((oy - radius + half) / tilesize) - 1);
        int maxX = Math.min(world.width() - 1,
                Mathf.floor((ox + radius + half) / tilesize) + 1);
        int maxY = Math.min(world.height() - 1,
                Mathf.floor((oy + radius + half) / tilesize) + 1);

        Scene scene = new Scene(
                minX, minY,
                Math.max(0, maxX - minX + 1),
                Math.max(0, maxY - minY + 1)
        );

        for(int y = 0; y < scene.height; y++){
            for(int x = 0; x < scene.width; x++){
                Building build = world.build(minX + x, minY + y);

                if(build != null && blocks.get(build)){
                    scene.cells[x + y * scene.width] = build;
                }
            }
        }

        return scene;
    }

    /**
     * Returns disjoint nearest contacts reached between from and to.
     * The caller must already have processed the front up to from.
     */
    public static void collectFaceHits(
            Scene scene,
            float ox, float oy, float from, float to,
            float segStart, float segLength,
            IntSet ignored,
            Seq<FaceHit> out
    ){
        out.clear();
        if(to <= from || segLength <= angleEpsilon) return;

        Seq<FaceHit> candidates = new Seq<>();

        for(int y = scene.y; y < scene.y + scene.height; y++){
            for(int x = scene.x; x < scene.x + scene.width; x++){
                Building build = scene.get(x, y, ignored);
                if(build == null) continue;

                for(int axis = 0; axis < 2; axis++){
                    for(int sign = -1; sign <= 1; sign += 2){
                        int nx = x + (axis == 0 ? sign : 0);
                        int ny = y + (axis == 1 ? sign : 0);

                        //No boundary inside the occupied union.
                        if(scene.get(nx, ny, ignored) != null) continue;

                        float plane = (axis == 0 ? x : y) * tilesize
                                + sign * tilesize / 2f;

                        float sourceCoord = axis == 0 ? ox : oy;
                        if((sourceCoord - plane) * sign <= 0f) continue;

                        float center = (axis == 0 ? y : x) * tilesize;

                        clipFace(
                                ox, oy, from, to,
                                segStart, segLength,
                                build, axis, plane,
                                center - tilesize / 2f,
                                center + tilesize / 2f,
                                sign > 0,
                                candidates
                        );
                    }
                }
            }
        }

        selectNearest(ox, oy, candidates, out);
    }

    /** Clips a finite face against the swept annulus. */
    private static void clipFace(
            float ox, float oy, float from, float to,
            float segStart, float segLength,
            Building build, int axis, float plane,
            float min, float max, boolean positive,
            Seq<FaceHit> out
    ){
        double fixed = plane - (axis == 0 ? ox : oy);
        double center = axis == 0 ? oy : ox;
        double fixed2 = fixed * fixed;
        double outer2 = (double)to * to;

        if(fixed2 > outer2) return;

        double outer = Math.sqrt(Math.max(0d, outer2 - fixed2));
        double low = Math.max(min, center - outer);
        double high = Math.min(max, center + outer);

        if(high <= low) return;

        double inner2 = (double)from * from;

        if(inner2 > fixed2){
            double inner = Math.sqrt(inner2 - fixed2);

            addFaceSpan(
                    ox, oy, segStart, segLength,
                    build, axis, plane,
                    low, Math.min(high, center - inner),
                    positive, out
            );

            addFaceSpan(
                    ox, oy, segStart, segLength,
                    build, axis, plane,
                    Math.max(low, center + inner), high,
                    positive, out
            );
        }else{
            addFaceSpan(
                    ox, oy, segStart, segLength,
                    build, axis, plane, low, high,
                    positive, out
            );
        }
    }

    /** Converts an already reached face span into angular intervals. */
    private static void addFaceSpan(
            float ox, float oy, float segStart, float segLength,
            Building build, int axis, float plane,
            double low, double high, boolean positive,
            Seq<FaceHit> out
    ){
        if(high <= low) return;

        double a = Math.toDegrees(Math.atan2(
                axis == 0 ? low - oy : plane - oy,
                axis == 0 ? plane - ox : low - ox
        ));

        double z = Math.toDegrees(Math.atan2(
                axis == 0 ? high - oy : plane - oy,
                axis == 0 ? plane - ox : high - ox
        ));

        double delta = z - a;
        delta -= Math.floor((delta + 180d) / 360d) * 360d;
        z = a + delta;

        if(z < a){
            double swap = a;
            a = z;
            z = swap;
        }

        double shift = Math.floor((segStart - a) / 360d) * 360d;
        double segEnd = (double)segStart + segLength;

        for(int i = 0; i < 2; i++){
            double start = Math.max(segStart, a + shift);
            double end = Math.min(segEnd, z + shift);

            if(end - start > angleEpsilon){
                out.add(new FaceHit(
                        build, build.team.id,
                        (float)start, (float)end,
                        axis, plane, positive
                ));
            }

            shift += 360d;
        }
    }

    /**
     * Boundary spans do not intersect except at endpoints.
     * Their depth order is therefore constant between angular endpoints.
     */
    private static void selectNearest(
            float ox, float oy,
            Seq<FaceHit> candidates,
            Seq<FaceHit> out
    ){
        if(candidates.isEmpty()) return;

        FloatSeq boundaries = new FloatSeq();

        for(int i = 0; i < candidates.size; i++){
            FaceHit hit = candidates.get(i);
            boundaries.add(hit.start, hit.end);
        }

        boundaries.sort();

        for(int i = 0; i < boundaries.size - 1; i++){
            float start = boundaries.items[i];
            float end = boundaries.items[i + 1];
            if(end - start <= angleEpsilon) continue;

            double mid = ((double)start + end) * 0.5d;
            FaceHit nearest = null;
            double nearestDistance = Double.POSITIVE_INFINITY;

            for(int j = 0; j < candidates.size; j++){
                FaceHit candidate = candidates.get(j);
                if(mid < candidate.start || mid > candidate.end) continue;

                double distance = distanceToPlane(
                        ox, oy, mid, candidate.axis, candidate.plane
                );

                if(distance < nearestDistance){
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }

            if(nearest == null) continue;

            if(!out.isEmpty()){
                FaceHit last = out.peek();

                if(last.build == nearest.build
                        && last.teamId == nearest.teamId
                        && last.axis == nearest.axis
                        && last.plane == nearest.plane
                        && last.positive == nearest.positive
                        && Math.abs(last.end - start) <= angleEpsilon){
                    last.end = end;
                    continue;
                }
            }

            out.add(new FaceHit(
                    nearest.build, nearest.teamId,
                    start, end,
                    nearest.axis, nearest.plane, nearest.positive
            ));
        }
    }

    public static double distanceToPlane(
            float ox, float oy, double angle,
            int axis, float plane
    ){
        double radians = Math.toRadians(angle);
        double direction = axis == 0
                ? Math.cos(radians)
                : Math.sin(radians);

        if(Math.abs(direction) < 1e-12d){
            return Double.POSITIVE_INFINITY;
        }

        return (plane - (axis == 0 ? ox : oy)) / direction;
    }

    /** Appends a wrapped interval clipped to one unwrapped segment. */
    public static void addClampedCut(
            Seq<Cut> out,
            float start, float end,
            float min, float max
    ){
        if(end - start <= angleEpsilon || max - min <= angleEpsilon) return;

        double shift = Math.floor(((double)min - start) / 360d) * 360d;

        for(int i = 0; i < 2; i++){
            float a = (float)Math.max(min, start + shift);
            float b = (float)Math.min(max, end + shift);

            if(b - a > angleEpsilon){
                out.add(new Cut(a, b));
            }

            shift += 360d;
        }
    }

    /** Writes surviving intervals. Does not know about bullet state. */
    public static void subtractCuts(
            float start, float length,
            Seq<Cut> cuts, Seq<Cut> out
    ){
        out.clear();

        float end = start + length;
        Seq<Cut> clipped = new Seq<>();

        for(int i = 0; i < cuts.size; i++){
            Cut cut = cuts.get(i);
            addClampedCut(clipped, cut.start, cut.end, start, end);
        }

        clipped.sort((a, b) -> Float.compare(a.start, b.start));

        float cursor = start;

        for(int i = 0; i < clipped.size; i++){
            Cut cut = clipped.get(i);
            if(cut.end <= cursor) continue;

            if(cut.start > cursor + angleEpsilon){
                out.add(new Cut(cursor, cut.start));
            }

            cursor = Math.max(cursor, cut.end);
            if(cursor >= end) break;
        }

        if(end - cursor > angleEpsilon){
            out.add(new Cut(cursor, end));
        }
    }
}