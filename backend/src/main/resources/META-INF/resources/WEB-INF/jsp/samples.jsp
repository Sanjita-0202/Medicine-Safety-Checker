<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Try samples - Medicine Safety Checker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="header.jsp"/>

<main class="page wide-page"><p class="eyebrow">Explore / Demo scenarios</p><div class="page-heading"><div><h1>Try it. See what happens.</h1><p class="subtitle">Ready-to-fill examples for the newer features. Sample buttons only fill the form; you choose when to run or save.</p></div><span class="demo-tag">Project demo data</span></div>
<div class="sample-grid">
<section class="card"><p class="card-label">01 / A simple approval</p><h2>A dose within the limits</h2><dl><dt>Medicine: </dt><dd>Amoxicillin</dd><dt>Dose: </dt><dd>500 mg, twice a day</dd><dt>Allergies / current medicines: </dt><dd>None</dd><dt>Expected: </dt><dd>SAFE</dd></dl><a class="button" href="${pageContext.request.contextPath}/check?scenario=safe">Try safe dose →</a></section>
<section class="card"><p class="card-label">02 / Catch an allergy</p><h2>Recognise a drug class</h2><dl><dt>Medicine: </dt><dd>Amoxicillin</dd><dt>Dose: </dt><dd>500 mg, twice a day</dd><dt>Allergy: </dt><dd>penicillin</dd><dt>Expected: </dt><dd>ALLERGY_CONFLICT</dd></dl><a class="button" href="${pageContext.request.contextPath}/check?scenario=allergy">Try allergy check →</a></section>
<section class="card"><p class="card-label">03 / Flag a high dose</p><h2>Spot the overdose risk</h2><dl><dt>Medicine: </dt><dd>Amoxicillin</dd><dt>Dose: </dt><dd>2,000 mg, twice a day</dd><dt>Allergies / current medicines: </dt><dd>None</dd><dt>Expected: </dt><dd>OVERDOSE_RISK</dd></dl><a class="button" href="${pageContext.request.contextPath}/check?scenario=overdose">Try dosage check →</a></section>
<section class="card"><p class="card-label">04 / Check the combination</p><h2>Look across medicines</h2><dl><dt>New medicine: </dt><dd>Levofloxacin, 500 mg once a day</dd><dt>Current medicine: </dt><dd>Aspirin</dd><dt>Allergies: </dt><dd>None</dd><dt>Expected: </dt><dd>DANGEROUS_COMBINATION</dd></dl><a class="button" href="${pageContext.request.contextPath}/check?scenario=interaction">Try interaction check →</a></section>
</div>
<section class="sample-banner" style="margin-top:24px"><h2>Try the complete patient journey</h2><p>Create Arjun using the sample patient button. Prescribe Amoxicillin at 500 mg twice a day, review the APPROVED history, then stop it and download the report. Create Maya to try a penicillin-allergy rejection.</p><div class="sample-buttons"><a href="${pageContext.request.contextPath}/patients/add?sample=general">Create Arjun</a><a href="${pageContext.request.contextPath}/patients/add?sample=allergy">Create Maya</a><a href="${pageContext.request.contextPath}/patients/add?sample=nsaid">Create Neha</a></div></section>
</main></body></html>
