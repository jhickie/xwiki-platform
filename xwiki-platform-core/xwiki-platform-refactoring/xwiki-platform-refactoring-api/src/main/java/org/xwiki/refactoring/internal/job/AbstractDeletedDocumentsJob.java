/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.refactoring.internal.job;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.Request;
import org.xwiki.job.api.AbstractCheckRightsRequest;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.refactoring.internal.ModelBridge;
import org.xwiki.refactoring.job.AbstractDeletedDocumentsRequest;
import org.xwiki.refactoring.job.RefactoringJobs;

/**
 * Define an abstract job that handles deleted documents.
 * Typically this class has been created to abstract mechanism of {@link RestoreJob} and {@link PermanentlyDeleteJob}.
 *
 * @version $Id$
 * @since 10.10RC1
 */
public abstract class AbstractDeletedDocumentsJob extends AbstractJob<AbstractDeletedDocumentsRequest,
    AbstractJobStatus<AbstractDeletedDocumentsRequest>> implements GroupedJob
{
    static final JobGroupPath ROOT_GROUP = new JobGroupPath(RefactoringJobs.GROUP, null);

    @Inject
    protected ModelBridge modelBridge;

    @Inject
    protected ModelContext modelContext;

    private JobGroupPath groupPath;

    @Override
    protected AbstractJobStatus<AbstractDeletedDocumentsRequest> createNewStatus(
        AbstractDeletedDocumentsRequest request)
    {
        AbstractJobStatus<AbstractDeletedDocumentsRequest> status = super.createNewStatus(request);

        status.setCancelable(true);

        return status;
    }

    @Override
    protected void runInternal() throws Exception
    {
        AbstractDeletedDocumentsRequest request = getRequest();

        initializeContext(request);

        this.progressManager.pushLevelProgress(2, this);

        this.progressManager.startStep(this);

        // Read the request and build the final list of IDs to restore.
        List<Long> idsToRestore = getIdsToHandle(request);

        this.progressManager.startStep(this);

        // Process each ID and try to restore it.
        handleDeletedDocuments(idsToRestore, request);

        this.progressManager.popLevelProgress(this);
    }

    protected abstract void handleDeletedDocuments(List<Long> idsDeletedDocuments, AbstractCheckRightsRequest request);

    private void initializeContext(AbstractDeletedDocumentsRequest request) throws IllegalArgumentException
    {
        // Set the context user to the one that made the request.
        DocumentReference userReference = request.getUserReference();
        modelBridge.setContextUserReference(userReference);

        // Set the context wiki to the one specified in the request.
        // All DeletedDocuments APIs, specifically the IDs (single document or batch), are local to the current wiki.
        WikiReference wikiReference = request.getWikiReference();
        // If the wiki is not specified, then we can not continue.
        if (wikiReference == null) {
            throw new IllegalArgumentException("No wiki reference was specified in the job request");
        }
        modelContext.setCurrentEntityReference(wikiReference);
    }

    private List<Long> getIdsToHandle(AbstractDeletedDocumentsRequest request)
    {
        List<Long> result = new ArrayList<>();

        // Expand the batch in individual deleted document IDs.
        String batchId = request.getBatchId();
        if (StringUtils.isNotBlank(batchId)) {
            List<Long> batchDeletedDocumentIds = modelBridge.getDeletedDocumentIds(batchId);
            result.addAll(batchDeletedDocumentIds);
        }

        // Merge any individually specified IDs, if they are not already in the batch.
        List<Long> deletedDocumentIds = request.getDeletedDocumentIds();
        if (deletedDocumentIds != null) {
            for (long deletedDocumentId : deletedDocumentIds) {
                if (!result.contains(deletedDocumentId)) {
                    result.add(deletedDocumentId);
                }
            }
        }

        return result;
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return groupPath;
    }

    @Override
    public void initialize(Request request)
    {
        super.initialize(request);

        // Build the job group path.
        // Note: Because of the nature of the AbstractDeletedDocumentsJobs that works with with DeletedDocument IDs and
        // BatchIDs (and not with EntityReferences), the only way we can try to avoid executing operation at the same
        // time on the same reference is to use a group path at the wiki level, hoping most restore operations are fast
        // and do not block for long.
        WikiReference wikiReference = ((AbstractDeletedDocumentsRequest) request).getWikiReference();
        if (wikiReference != null) {
            this.groupPath = new JobGroupPath(wikiReference.getName(), ROOT_GROUP);
        }
    }
}
