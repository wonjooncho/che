/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * File open/close event listener aimed to wrap {@link FileEvent} into {@code FileTrackingEvent}
 * which is consumed by {@link ClientServerEventService} and sent to server side for further
 * processing.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileOpenCloseEventListener {

  @Inject
  public FileOpenCloseEventListener(
      final Provider<EditorAgent> editorAgentProvider,
      final EventBus eventBus,
      final ClientServerEventService clientServerEventService) {

    Log.debug(getClass(), "Adding file event listener");
    eventBus.addHandler(
        FileEvent.TYPE,
        new FileEvent.FileEventHandler() {
          @Override
          public void onFileOperation(FileEvent event) {
            VirtualFile file = event.getFile();
            if (!(file instanceof Resource)) {
              return;
            }

            Path path = file.getLocation();
            EditorAgent editorAgent = editorAgentProvider.get();

            switch (event.getOperationType()) {
              case OPEN:
                {
                  processFileOpen(path);
                  break;
                }
              case CLOSE:
                {
                  final EditorPartPresenter closingEditor =
                      event.getEditorTab().getRelativeEditorPart();
                  final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

                  processFileClose(closingEditor, openedEditors, path);

                  break;
                }
              default:
            }
          }

          private void processFileOpen(Path path) {
            clientServerEventService.sendFileTrackingStartEvent(path.toString());
          }

          private void processFileClose(
              EditorPartPresenter closingEditor,
              List<EditorPartPresenter> openedEditors,
              Path path) {
            for (final EditorPartPresenter editor : openedEditors) {
              final Path editorFilePath = editor.getEditorInput().getFile().getLocation();
              if (Objects.equals(path, editorFilePath) && closingEditor != editor) {
                return;
              }
            }

            clientServerEventService.sendFileTrackingStopEvent(path.toString());
          }
        });
  }
}
