(ns tentacles.repos
  "Implements the Github Repos API: http://developer.github.com/v3/repos/"
  (:use [clj-http.client :only [post]]
        [clojure.java.io :only [file]]
        [tentacles.core :only [api-call]]
        [slingshot.slingshot :only [try+]]))

;; ## Primary Repos API

(defn repos
  "List the authenticated user's repositories.
   Options are:
      type -- all (default), public, private, member."
  [options]
  (api-call :get "user/repos" nil options))

(defn user-repos
  "List a user's repositories.
   Options are:
      types -- all (default), public, private, member."
  [user & [options]]
  (api-call :get "users/%s/repos" [user] options))

(defn org-repos
  "List repositories for an organization.
   Options are:
      type -- all (default), public, private."
  [org & [options]]
  (api-call :get "orgs/%s/repos" [org] options))

(defn create-repo
  "Create a new repository.
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false."
  [name options]
  (api-call :post "user/repos" nil (assoc options :name name)))

(defn create-org-repo
  "Create a new repository in an organization..
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false.
      team-id       -- Team that will be granted access to this
                       repository."
  [org name options]
  (api-call :post "orgs/%s/repos" [org] (assoc options :name name)))

(defn specific-repo
  "Get a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s" [user repo] options))

(defn edit-repo
  "Edit a repository.
   Options are:
      description   -- Repository's description.
      name          -- Repository's name.
      homepage      -- Link to repository's homepage.
      public        -- true, false.
      has-issues    -- true, false.
      has-wiki      -- true, false.
      has-downloads -- true, false."
  [user repo options]
  (api-call :post "repos/%s/%s"
            [user repo]
            (if (:name options)
              options
              (assoc options :name repo))))

(defn contributors
  "List the contributors for a project.
   Options are:
      anon -- true, false (default): If true, include
              anonymous contributors."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/contributors" [user repo] options))

(defn languages
  "List the languages that a repository uses."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/languages" [user repo] options))

(defn teams
  "List a repository's teams."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/teams" [user repo] options))

(defn tags
  "List a repository's tags."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/tags" [user repo] options))

(defn branches
  "List a repository's branches."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/branches" [user repo] options))

;; ## Repo Collaborators API

(defn collaborators
  "List a repository's collaborators."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/collaborators" [user repo] options))

(defn collaborator?
  "Check if a user is a collaborator."
  [user repo collaborator & [options]]
  (try+
   (nil? (api-call :get "repos/%s/%s/collaborators/%s" [user repo collaborator] options))
   (catch [:status 404] _ false)))

(defn add-collaborator
  "Add a collaborator to a repository."
  [user repo collaborator options]
  (nil? (api-call :put "repos/%s/%s/collaborators/%s" [user repo collaborator] options)))

(defn remove-collaborator
  "Remove a collaborator from a repository."
  [user repo collaborator options]
  (nil? (api-call :delete "repos/%s/%s/collaborators/%s" [user repo collaborator] options)))

;; ## Repo Commits API

(defn commits
  "List commits for a repository.
   Options are:
      sha  -- Sha or branch to start lising commits from.
      path -- Only commits at this path will be returned."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/commits" [user repo] options))

(defn specific-commit
  "Get a specific commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/commits/%s" [user repo sha] options))

(defn commit-comments
  "List the commit comments for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/comments" [user repo] options))

(defn specific-commit-comments
  "Get the comments on a specific commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/commits/%s/comments" [user repo sha] options))

;; 'line' is supposed to be a required argument for this API call, but
;; I'm convinced that it doesn't do anything. The only thing that seems
;; to matter is the 'position' argument. As a matter of fact, we can omit
;; 'line' entirely and Github does not complain, despite it supposedly being
;; a required argument.
;;
;; Furthermore, it requires that the sha be passed in the URL *and* the JSON
;; input. I don't see how they can ever possibly be different, so we're going
;; to just require one sha.
(defn create-commit-comment
  "Create a commit comment. path is the location of the file you're commenting on.
   position is the index of the line you're commenting on. Not the actual line number,
   but the nth line shown in the diff."
  [user repo sha path position body options]
  (api-call :post "repos/%s/%s/commits/%s/comments" [user repo sha]
            (assoc options
              :body body
              :commit-id sha
              :path path
              :position position)))

(defn specific-commit-comment
  "Get a specific commit comment."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/comments/%s" [user repo id] options))

(defn update-commit-comment
  "Update a commit comment."
  [user repo id body options]
  (api-call :post "repos/%s/%s/comments/%s" [user repo id] (assoc options :body body)))

(defn compare-commits
  [user repo base head & [options]]
  (api-call :get "repos/%s/%s/compare/%s...%s" [user repo base head] options))

(defn delete-commit-comment
  [user repo id options]
  (nil? (api-call :delete "repos/%s/%s/comments/%s" [user repo id] options)))

;; ## Repo Downloads API

(defn downloads
  "List the downloads for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/downloads" [user repo] options))

(defn specific-download
  "Get a specific download."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/downloads/%s" [user repo id] options))

(defn delete-download
  "Delete a download"
  [user repo id options]
  (nil? (api-call :delete "repos/%s/%s/downloads/%s" [user repo id] options)))

;; Commenting this stuff out. It doesn't actually work yet because
;; we need multipart/form-data.
(comment
  (defn download-resource [user repo path options]
    (let [path (file path)]
      (assoc (api-call :post "repos/%s/%s/downloads"
                       [user repo]
                       (assoc options
                         :name (.getName path)
                         :size (.length path)))
        :filepath path)))

  (defn create-download
    "Creates a new download."
    [resp]
    (post (:s3_url resp)
          {:debug true
           :form-params [["key" (:path resp)]
                         ["acl" (:acl resp)]
                         ["success_action_status" "201"]
                         ["Filename" (:name resp)]
                         ["AWSAccessKeyId" (:accesskeyid resp)]
                         ["Policy" (:policy resp)]
                         ["Signature" (:signature resp)]
                         ["Content-Type" (:mime_type resp)]
                         ["file" (slurp (:filepath resp))]]})))