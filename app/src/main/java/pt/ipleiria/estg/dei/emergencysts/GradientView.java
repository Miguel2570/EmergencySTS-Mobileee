package pt.ipleiria.estg.dei.emergencysts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GradientView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient gradient;
    private int[] colors = {
            0xFF57B894, // tom mais escuro (mais contraste)
            0xFFA7D7C5, // tom médio
            0xFFE4FFF5  // tom claro
    };

    private float translate = 0f;
    private int w = 0;
    private int h = 0;
    private ValueAnimator animator;

    public GradientView(Context context) {
        this(context, null);
    }

    public GradientView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        initAnimator();
    }

    private void initAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(5000); // ajusta se quiseres mais rápido/lento
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> {
            translate = (float) a.getAnimatedValue();
            updateGradient(); // atualiza o shader aqui (fora do onDraw)
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null && !animator.isRunning()) animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        updateGradient();
    }

    public void setColors(int[] newColors) {
        if (newColors == null || newColors.length == 0) return;
        this.colors = newColors.clone();
        updateGradient();
        invalidate();
    }

    private void updateGradient() {
        if (w == 0 || h == 0) return;

        // Faz o deslocamento variar entre -0.5w .. +0.5w e -0.5h .. +0.5h
        float norm = translate * 2f - 1f;
        float offsetX = norm * (w * 0.5f);
        float offsetY = norm * (h * 0.5f);

        float startX = -offsetX;
        float startY = -offsetY;
        float endX = w + offsetX;
        float endY = h + offsetY;

        // CLAMP evita padrões repetidos; altera se preferires MIRROR/REPEAT
        gradient = new LinearGradient(startX, startY, endX, endY, colors, null, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gradient == null) updateGradient();
        canvas.drawRect(0, 0, w, h, paint);
    }
}