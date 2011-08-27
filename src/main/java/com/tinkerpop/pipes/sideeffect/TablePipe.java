package com.tinkerpop.pipes.sideeffect;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.PipeClosure;
import com.tinkerpop.pipes.util.AsPipe;
import com.tinkerpop.pipes.util.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TablePipe<S> extends AbstractPipe<S, S> implements SideEffectPipe<S, Table> {

    private Table table;
    private final PipeClosure[] closures;
    private int currentClosure;
    private final List<AsPipe> asPipes = new ArrayList<AsPipe>();
    private final boolean doClosures;

    public TablePipe(final Table table, final Collection<String> columnNames, final List<AsPipe> allPreviousAsPipes, final PipeClosure... closures) {
        this.table = table;
        this.closures = closures;

        if (this.doClosures = this.closures.length > 0)
            currentClosure = 0;

        final List<String> tempNames = new ArrayList<String>();
        for (final AsPipe asPipe : allPreviousAsPipes) {
            final String columnName = asPipe.getName();
            if (null == columnNames || columnNames.contains(columnName)) {
                tempNames.add(columnName);
                this.asPipes.add(asPipe);
            }
        }

        if (tempNames.size() > 0)
            table.setColumnNames(tempNames.toArray(new String[tempNames.size()]));

    }

    public Table getSideEffect() {
        return this.table;
    }

    public S processNextStart() {
        final S s = this.starts.next();
        final List row = new ArrayList();
        for (final AsPipe asPipe : this.asPipes) {
            if (doClosures) {
                row.add(this.closures[currentClosure++ % closures.length].compute(asPipe.getCurrentEnd()));
            } else {
                row.add(asPipe.getCurrentEnd());
            }
        }
        this.table.addRow(row);
        return s;
    }

    public void reset() {
        this.table = new Table();
        this.currentClosure = 0;
        super.reset();
    }
}