package com.eaglesakura.android.camera.spec;

import com.eaglesakura.math.Vector2;

/**
 * 撮影・プレビュー用のサイズを返す
 */
public class CaptureSize {
    private final Vector2 mSize;

    private final Aspect mAspectID;

    public enum Aspect {
        /**
         * 縦横1:1
         */
        WH1x1 {
            @Override
            public double aspect() {
                return 1;
            }

            @Override
            public String aspectText() {
                return "1:1";
            }
        },

        /**
         * 縦横3x2
         */
        WH3x2 {
            @Override
            public double aspect() {
                return 3.0 / 2.0;
            }

            @Override
            public String aspectText() {
                return "3:2";
            }
        },
        /**
         * 縦横4:3
         */
        WH4x3 {
            @Override
            public double aspect() {
                return 4.0 / 3.0;
            }

            @Override
            public String aspectText() {
                return "4:3";
            }
        },

        /**
         * 縦横16:9
         */
        WH16x9 {
            @Override
            public double aspect() {
                return 16.0 / 9.0;
            }

            @Override
            public String aspectText() {
                return "16:9";
            }
        },

        /**
         * 縦横16:10
         */
        WH16x10 {
            @Override
            public double aspect() {
                return 16.0 / 10.0;
            }

            @Override
            public String aspectText() {
                return "16:10";
            }
        };

        /**
         * 横ピクセル数 / 縦ピクセル数のアスペクト比を取得する
         */
        public abstract double aspect();

        /**
         * アスペクト比のテキストを取得する
         * <br>
         * 例：16:9
         */
        public abstract String aspectText();

        /**
         * 最も近いアスペクト比を取得する
         */
        static Aspect getNearAspect(double aspect) {
            double diffNear = 99999999;
            Aspect result = null;

            Aspect[] values = values();
            for (Aspect value : values) {
                final double checkDiff = Math.abs(value.aspect() - aspect);
                if (checkDiff < diffNear) {
                    // 差が小さいならコレにする
                    result = value;
                    // 次はコレが比較対象
                    diffNear = checkDiff;
                }
            }
            return result;
        }
    }

    public CaptureSize(int width, int height) {
        this.mSize = new Vector2(width, height);
        this.mAspectID = Aspect.getNearAspect(getAspect());
    }

    /**
     * ピクセル数をメガピクセル単位で取得する
     *
     * @return 計算されたメガピクセル
     */
    public double getMegaPixel() {
        return ((double) (mSize.x * mSize.y)) / 1000.0 / 1000.0;
    }

    public int getWidth() {
        return (int) mSize.x;
    }

    public int getHeight() {
        return (int) mSize.y;
    }

    /**
     * ユーザー表示用のメガピクセル数を取得する。
     * <br>
     * 小数点第一位まで計算する
     * <br>
     * 例) 5.0
     * <br>
     * 例)13.1
     *
     * @return 表示用のメガピクセル
     */
    public String getMegaPixelText() {
        return String.format("%.1f", getMegaPixel());
    }

    /**
     * アスペクト比表示用テキストを取得する
     * 例) 16:9
     */
    public String getAspectText() {
        return mAspectID.aspectText();
    }

    /**
     * アスペクト比のIDを取得する
     */
    public Aspect getAspectType() {
        return mAspectID;
    }

    /**
     * アスペクト比を取得する
     */
    public double getAspect() {
        return (double) getWidth() / (double) getHeight();
    }

    /**
     * 一意に識別するためのIDを取得する
     */
    public String getId() {
        return String.format("pic(%dx%d)", getWidth(), getHeight());
    }
}
