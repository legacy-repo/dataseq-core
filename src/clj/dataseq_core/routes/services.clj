(ns dataseq-core.routes.services
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [dataseq-core.middleware.formats :as formats]
   [dataseq-core.middleware.exception :as exception]
   [ring.util.http-response :refer [ok]]
   [dataseq-core.routes.data-commons :refer [data-commons]]))

(defn service-routes []
  (merge
   ["/api"
    {:coercion spec-coercion/coercion
     :muuntaja formats/instance
     :swagger {:id ::api}
     :middleware [;; query-params & form-params
                  parameters/parameters-middleware
                 ;; content-negotiation
                  muuntaja/format-negotiate-middleware
                 ;; encoding response body
                  muuntaja/format-response-middleware
                 ;; exception handling
                  exception/exception-middleware
                 ;; decoding request body
                  muuntaja/format-request-middleware
                 ;; coercing response bodys
                  coercion/coerce-response-middleware
                 ;; coercing request parameters
                  coercion/coerce-request-middleware
                 ;; multipart
                  multipart/multipart-middleware]}

    ;; swagger documentation
    ["" {:no-doc true
         :swagger {:info {:title "Data Seq for Omics Data Commons"
                          :description "https://cljdoc.org/d/metosin/reitit"}}}

     ["/swagger.json"
      {:get (swagger/create-swagger-handler)}]

     ["/api-docs/*"
      {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

    ["" {:swagger {:tags ["Utility"]}}
     ["/ping"
      {:get (constantly (ok {:message "pong"}))}]]]

   ;; The group of routes for data commons
   data-commons))
