/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.test;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.FileService;
import us.physion.ovation.api.*;
import us.physion.ovation.couch.CouchServiceManager;
import us.physion.ovation.couch.OvationCouchModule;
import us.physion.ovation.database.DatabaseCoordinator;
import us.physion.ovation.domain.Group;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.User;
import us.physion.ovation.domain.dao.EntityDao;
import us.physion.ovation.domain.dto.EntityBase;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.exceptions.UserAccessException;
import us.physion.ovation.validation.ValidationResult;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.OvationWebApi;

/**
 *
 * @author huecotanks
 */
public class OvationTestCase {

    public OvationTestCase() {
    }
    public DataStoreCoordinator dsc;
    public final static UUID USER_UUID = UUID.randomUUID();
    public final static String EMAIL = "email@email.com";//"me@bobslawblog.com";
    public final static String USER_NAME = "jackie";
    public final static char[] PASSWORD = "password".toCharArray();//"poorPasswordChoice".toCharArray();
    public final static String UNUSED_KEY = "UNUSED KEY";
    public final static String CLOUDANT_SERVER = "http://localhost:5995/ovation-mydb"; /*
     * String.format("http://%s:%s/%s", OvationCouchModule.COUCH_HOST,
     * OvationCouchModule.COUCH_PORT,
            "mydb");
     */

    Injector injector;

    @Before
    public void setUp() {
        com.google.inject.Module apiOverride = Modules.override(new OvationApiModule()).with(new AbstractModule() {

            @Override
            protected void configure() {
                bind(OvationWebApi.class).to(FakeWebApi.class);
                bind(FileService.class).to(FakeFileService.class);
                //Override cloud provider to use transient storage
                bindConstant().annotatedWith(Names.named("cloud.provider")).to("transient");
            }
        });
        injector = Guice.createInjector(apiOverride);
        dsc = injector.getInstance(DataStoreCoordinator.class);
        try {
            setup_cloud();
            dsc.authenticateUser(EMAIL, "password".toCharArray());
            //createUser();
        } catch (InterruptedException ex) {
            Logger.getLogger(OvationTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OvationTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(OvationTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Injector getInjector() {
        return injector;
    }

    public void setup_cloud() throws InterruptedException, IOException, ExecutionException {
        /*String userIdentity = EMAIL;
        final String databaseName = userIdentity.replace(".", "-").replace("@", "-");

        CouchServiceManager m = injector.getInstance(CouchServiceManager.class);
        if (!m.serviceAvailable()) {
            m.startService();
        }

        final String cloudDatabaseName = databaseName + "-cloud";

        LocalDatabaseStack databaseStack = injector.getInstance(LocalDatabaseStack.class);
        ListenableFuture<DatabaseCoordinator> cloudUriFuture = databaseStack.createLocalCloudDatabase(cloudDatabaseName,
                userIdentity,
                "password".toCharArray(),
                USER_NAME,//OvationCouchModule.COUCH_PROCESS_OWNER,
                UUID.randomUUID(),
                null);

        //Wait for the stack to be built
        cloudUriFuture.get();
        *
        */
    }

    @After
    public void tearDown() throws InterruptedException {
        if (dsc != null) {
            dsc.deleteDB();
            //queryService.reset();
            Thread.sleep(1000);
        }
    }
/*
    protected us.physion.ovation.domain.dto.User makeNewUser() {
        return new us.physion.ovation.domain.dto.User() {

            @Override
            public String getUsername() {
                return USER_NAME;
            }

            @Override
            public String getEmail() {
                return EMAIL;
            }

            @Override
            public char[] getPasswordHash() {
                return new char[]{1};
            }

            @Override
            public String getDigestAlgorithm() {
                return "SHA-256";
            }

            @Override
            public int getPkcs5Iterations() {
                return 5;
            }

            @Override
            public char[] getPasswordSalt() {
                return new char[]{'s'};
            }

            @Override
            public char[] getPasswordPepper() {
                return new char[]{'p'};
            }

            @Override
            public UUID getUuid() {
                return USER_UUID;
            }

            @Override
            public String getRevision() {
                return null;
            }

            @Override
            public Set<UUID> getWriteGroups() {
                return Sets.newHashSet();
            }

            @Override
            public List<String> getConflicts() {
                return new ArrayList();
            }

            @Override
            public Class getEntityClass() {
                return us.physion.ovation.domain.User.class;
            }

            @Override
            public PersistentComponentUpdate getPersistentComponentsForUpdate(EntityDao arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void fetchPersistentComponents(EntityDao arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public void createUser() {
        us.physion.ovation.domain.dto.User u = makeNewUser();
        try {
            boolean b = dsc.authenticateUser(u.getEmail(), "password".toCharArray()).get();
            dsc.getDatabaseCoordinator().getDao().save(u);
            if (!dsc.authenticateUser(u.getEmail(), "password".toCharArray()).get()) {
                dsc = null;
                throw new OvationException("Unable to authenticate data context");
            }
        } catch (InterruptedException e) {
            throw new OvationException("Unable to authenticate data context");
        } catch (ExecutionException e) {
            throw new OvationException("Unable to authenticate data context");
        }

    }*/

    static class FakeWebApi implements OvationWebApi {

        Map<String, String> emailLookup;

        FakeWebApi() {
            emailLookup = new HashMap();
            emailLookup.put(EMAIL, USER_UUID.toString());
        }

        public void addUser(String email, UUID user) {
            emailLookup.put(email, user.toString());
        }
        
        public ListenableFuture<Map<String, String>> authenticateUser(String email, String password, long l, TimeUnit tu) {
            Map<String, String> response = new HashMap();

            if (!emailLookup.keySet().contains(email)) {
                response.put(OvationWebApi.STATUS_KEY, "54");
                return Futures.immediateFuture(response);
            }

            response.put(OvationWebApi.STATUS_KEY, "200");
            response.put(OvationWebApi.BLOB_IDENTITY_KEY, EMAIL);
            response.put(OvationWebApi.BLOB_PASSWORD_KEY, "password");
            response.put(OvationWebApi.BLOB_PASSWORD_KEY, "file:/Users/jackie/something");
            response.put(OvationWebApi.CLOUDANT_PASSWORD_KEY, "118i5r4bem1s7g2k6u2f3bca6k");
            response.put(OvationWebApi.CLOUDANT_IDENTITY_KEY, "jackie");
            response.put(OvationWebApi.CLOUDANT_URI_KEY, "http://localhost:5995/email-email-com-cloud");
            response.put(OvationWebApi.USER_UUID_KEY, emailLookup.get(email));
            return Futures.immediateFuture(response);
        }
    }

    static class FakeFileService implements FileService {

        public UploadResult pushToBlobStorage(final UUID owner, UUID transactionId, final URL content) {
            return new UploadResult() {

                @Override
                public ListenableFuture<URL> getStagedContentUrl() {
                    return Futures.immediateFuture(content);
                }

                @Override
                public ListenableFuture<String> getEtag() {
                    return Futures.immediateFuture("my-etag");
                }

                @Override
                public ListenableFuture<URI> getCloudUri() {
                    try {
                        return Futures.immediateFuture(content.toURI());
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(OvationTestCase.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                }

                @Override
                public UUID getOwnerId() {
                    return owner;
                }
            };
        }

        @Override
        public ListenableFuture<File> getLocalFile(URI content, String etag) {
            return Futures.immediateFuture(new File(content.getPath()));
        }

        public Iterable<UploadResult> pushAllPendingToBlobStorage() {
            return Sets.newHashSet();
        }

        @Override
        public UploadResult pushToCloudStorage(UUID arg0, UUID arg1, URL arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterable<UploadResult> pushAllPendingToCloudStorage() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
