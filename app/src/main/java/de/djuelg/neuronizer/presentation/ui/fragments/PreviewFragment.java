package de.djuelg.neuronizer.presentation.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.djuelg.neuronizer.R;
import de.djuelg.neuronizer.domain.executor.impl.ThreadExecutor;
import de.djuelg.neuronizer.domain.model.TodoListPreview;
import de.djuelg.neuronizer.presentation.presenters.PreviewPresenter;
import de.djuelg.neuronizer.presentation.presenters.impl.PreviewPresenterImpl;
import de.djuelg.neuronizer.presentation.ui.custom.FlexibleRecyclerView;
import de.djuelg.neuronizer.presentation.ui.custom.FragmentInteractionListener;
import de.djuelg.neuronizer.presentation.ui.flexibleAdapter.TodoListPreviewUI;
import de.djuelg.neuronizer.storage.PreviewRepositoryImpl;
import de.djuelg.neuronizer.threading.MainThreadImpl;
import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Activities that contain this fragment must implement the
 * {@link FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewFragment extends Fragment implements PreviewPresenter.View, View.OnClickListener, FlexibleAdapter.OnItemClickListener {

    @Bind(R.id.fab_add_list) FloatingActionButton mFabButton;
    @Bind(R.id.preview_recycler_view) FlexibleRecyclerView mRecyclerView;
    @Bind(R.id.preview_empty_recycler_view) RelativeLayout mEmptyView;

    private PreviewPresenter mPresenter;
    private FragmentInteractionListener mListener;
    private FlexibleAdapter<TodoListPreviewUI> mAdapter;

    public PreviewFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment PreviewFragment.
     */
    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create a presenter for this view
        mPresenter = new PreviewPresenterImpl(
                ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(),
                this,
                new PreviewRepositoryImpl()
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_preview, container, false);

        ButterKnife.bind(this, view);
        mFabButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // let's start welcome message retrieval when the app resumes
        mPresenter.resume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showNoPreviewsExisting() {
        List<TodoListPreviewUI> emptyUI = new ArrayList<>();
        setupUIComponents(emptyUI);
    }

    @Override
    public void displayPreviews(Iterable<TodoListPreview> previews) {
        List<TodoListPreviewUI> previewUIs = new ArrayList<>();
        for (TodoListPreview preview : previews) {
            previewUIs.add(new TodoListPreviewUI(preview));
        }

        setupUIComponents(previewUIs);
    }

    private void setupUIComponents(List<TodoListPreviewUI> previewUIs) {
        mAdapter = new FlexibleAdapter<>(previewUIs);
        mRecyclerView.setupFlexibleAdapter(this, mAdapter);
        mRecyclerView.setupRecyclerView(mEmptyView, mAdapter, mFabButton);
        mAdapter.setSwipeEnabled(true);
        mAdapter.getItemTouchHelperCallback().setSwipeThreshold(0.666F);
    }

    @Override
    public void onClick(View view) {
        // Currently there is only FAB
        switch (view.getId()) {
            case R.id.fab_add_list:
                mListener.onAddTodoList();
                break;
        }
    }

    @Override
    public boolean onItemClick(int position) {
        TodoListPreviewUI previewUI = mAdapter.getItem(position);
        if (previewUI != null) {
            mListener.onTodoListSelected(previewUI.getTodoListUuid(), previewUI.getTodoListTitle());
            return true;
        }
        return false;
    }
}
