package pylapp.smoothclicker.android.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import pylapp.smoothclicker.android.R;

public class IntroScreensActivity extends AppCompatActivity {

    private int currentPage = 0;
    private String[] titles;
    private String[] summaries;
    private int[] images = {
            R.drawable.smooth_clicker,
            R.drawable.root,
            R.drawable.clicks,
            R.drawable.notifications,
            R.drawable.json,
            R.drawable.standalone,
            R.drawable.screenrecognition,
            R.drawable.open_sources
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        titles = getResources().getStringArray(R.array.introscreen_titles);
        summaries = getResources().getStringArray(R.array.introscreen_summaries);

        updatePage();

        Button btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < titles.length - 1) {
                    currentPage++;
                    updatePage();
                } else {
                    startMainActivity();
                }
            }
        });
    }

    private void updatePage() {
        TextView title = findViewById(R.id.intro_title);
        TextView description = findViewById(R.id.intro_description);
        ImageView image = findViewById(R.id.intro_image);
        LinearLayout main = findViewById(R.id.intro_main);
        Button btnNext = findViewById(R.id.btnNext);

        title.setText(titles[currentPage]);
        description.setText(summaries[currentPage]);
        image.setImageResource(images[currentPage]);
        main.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));

        if (currentPage == titles.length - 1) {
            btnNext.setText(R.string.action_done);
        } else {
            btnNext.setText(R.string.action_next);
        }

        updateIndicators();
    }

    private void updateIndicators() {
        LinearLayout indicators = findViewById(R.id.intro_indicators);
        indicators.removeAllViews();

        for (int i = 0; i < titles.length; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.intro_indicator_size),
                    getResources().getDimensionPixelSize(R.dimen.intro_indicator_size)
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(
                    i == currentPage ? R.drawable.indicator_active : R.drawable.indicator_inactive
            );
            indicators.addView(indicator);
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(IntroScreensActivity.this, ClickerActivity.class);
        startActivity(i);
        finish();
    }
}