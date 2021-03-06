/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.search.model;

import java.util.List;

public class ProductSearchResults {
    private String indexTime;
    private List<Result> results;
    private List<ProductGroupedResult> productGroupedResults;

    public String getIndexTime() {
        return indexTime;
    }

    public void setIndexTime(String indexTime) {
        this.indexTime = indexTime;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<ProductGroupedResult> getProductGroupedResults() {
        return productGroupedResults;
    }

    public void setProductGroupedResults(List<ProductGroupedResult> productGroupedResults) {
        this.productGroupedResults = productGroupedResults;
    }
}
