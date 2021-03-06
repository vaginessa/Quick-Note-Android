package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.formats.NoteType;
import com.bijoysingh.quicknote.recyclerview.SimpleItemTouchHelper;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.views.ColorView;

import java.util.Collections;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity.HANDLER_UPDATE_TIME;

public class CreateOrEditAdvancedNoteActivity extends ViewAdvancedNoteActivity {

  private boolean active = false;
  private int maxUid = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTouchListener();
    startHandler();
    setColorsList();
  }

  @Override
  protected void setEditMode() {
    setEditMode(true);
  }

  private void setTouchListener() {
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelper(adapter);
    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
    touchHelper.attachToRecyclerView(formatsView);
  }

  @Override
  protected void setNote() {
    super.setNote();
    maxUid = formats.size() + 1;
    boolean isEmpty = formats.isEmpty();
    if (isEmpty || formats.get(0).formatType != FormatType.HEADING) {
      addEmptyItem(0, FormatType.HEADING);
    }
    if (isEmpty) {
      addDefaultItem();
    }
  }

  protected void addDefaultItem() {
    addEmptyItem(FormatType.TEXT);
  }

  @Override
  protected void setButtonToolbar() {
    ImageView text = (ImageView) findViewById(R.id.format_text);
    text.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.TEXT);
      }
    });

    ImageView heading = (ImageView) findViewById(R.id.format_heading);
    heading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.HEADING);
      }
    });

    ImageView subHeading = (ImageView) findViewById(R.id.format_sub_heading);
    subHeading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.SUB_HEADING);
      }
    });

    ImageView checkList = (ImageView) findViewById(R.id.format_check_list);
    checkList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED);
      }
    });

    ImageView quote = (ImageView) findViewById(R.id.format_quote);
    quote.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.QUOTE);
      }
    });

    ImageView code = (ImageView) findViewById(R.id.format_code);
    code.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CODE);
      }
    });
  }

  @Override
  protected void setTopToolbar() {
    actionDelete.setVisibility(GONE);
    actionPopUp.setVisibility(GONE);
    actionShare.setVisibility(GONE);
    actionCopy.setVisibility(GONE);

    View colorButtonClicker = findViewById(R.id.color_button_clicker);
    colorButtonClicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean isVisible = colorSelectorLayout.getVisibility() == VISIBLE;
        colorSelectorLayout.setVisibility(isVisible ? GONE : VISIBLE);
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    active = false;
    maybeUpdateNote();
  }

  @Override
  public void onBackPressed() {
    active = false;
    maybeUpdateNote();
    destroyIfNeeded();
    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();
    active = true;
  }

  @Override
  protected void onResumeAction() {
    // do nothing
  }

  private boolean destroyIfNeeded() {
    if (note.isUnsaved()) {
      return true;
    }
    if (note.getFormats().isEmpty()) {
      note.delete(this);
      return true;
    }
    return false;
  }

  @Override
  protected void maybeUpdateNote() {
    note.title = NoteType.RICH_NOTE.name();
    updateNote();
  }

  private void startHandler() {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (active) {
          maybeUpdateNote();
          handler.postDelayed(this, HANDLER_UPDATE_TIME);
        }
      }
    }, HANDLER_UPDATE_TIME);
  }


  protected void addEmptyItem(FormatType type) {
    addEmptyItem(formats.size(), type);
  }

  private void addEmptyItem(int position, FormatType type) {
    Format format = new Format(type);
    format.uid = maxUid + 1;
    maxUid++;

    formats.add(position, format);
    adapter.addItem(format, position);
  }

  private void addEmptyItemAtFocused(FormatType type) {
    if (focusedFormat == null) {
      addEmptyItem(type);
      return;
    }

    int position = getFormatIndex(focusedFormat);
    if (position == -1) {
      addEmptyItem(type);
      return;
    }

    addEmptyItem(position + 1, type);
  }

  private void setColorsList() {
    colorSelectorLayout.removeAllViews();
    int[] colors = getResources().getIntArray(R.array.bright_colors);
    for (final int color : colors) {
      ColorView item = new ColorView(this);
      item.setColor(color, note.color == color);
      item.root.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          setNoteColor(color);
          setColorsList();
          colorSelectorLayout.setVisibility(GONE);
        }
      });
      colorSelectorLayout.addView(item);
    }
  }

  @Override
  protected void setNoteColor(int color) {
    note.color = color;
    colorButton.setBackground(new CircleDrawable(note.color));
  }

  @Override
  public void setFormat(Format format) {
    int position = getFormatIndex(format);
    if (position == -1) {
      return;
    }
    formats.set(position, format);
  }

  @Override
  public void moveFormat(int fromPosition, int toPosition) {
    if (fromPosition < toPosition) {
      for (int i = fromPosition; i < toPosition; i++) {
        Collections.swap(formats, i, i + 1);
      }
    } else {
      for (int i = fromPosition; i > toPosition; i--) {
        Collections.swap(formats, i, i - 1);
      }
    }
    maybeUpdateNote();
  }

  @Override
  public void deleteFormat(Format format) {
    int position = getFormatIndex(format);
    if (position <= 0) {
      return;
    }
    focusedFormat = focusedFormat == null || focusedFormat.uid == format.uid ? null : focusedFormat;
    formats.remove(position);
    adapter.removeItem(position);
    maybeUpdateNote();
  }

  @Override
  public void setFormatChecked(Format format, boolean checked) {
    // do nothing
  }
}
