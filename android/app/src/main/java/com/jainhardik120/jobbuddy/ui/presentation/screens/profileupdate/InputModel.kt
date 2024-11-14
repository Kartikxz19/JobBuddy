package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

sealed class InputModel(
    var fields: Map<String, InputFieldType>
) {
    class Skill(skill: String, level: Int) : InputModel(
        fields = mapOf(
            "skill" to InputFieldType("Skill", InputType.Text(skill)),
            "level" to InputFieldType("Level", InputType.Number(level))
        )
    )

    class Project(
        name: String,
        techStack: String,
        demoLink: String,
        startDate: InputType.Date,
        endDate: InputType.Date,
        description: String
    ) : InputModel(
        fields = mapOf(
            "name" to InputFieldType("Name", InputType.Text(name)),
            "techStack" to InputFieldType("Tech Stack", InputType.Text(techStack)),
            "demoLink" to InputFieldType("Demo Link", InputType.Text(demoLink)),
            "startDate" to InputFieldType(
                "Start Date",
                InputType.Date(startDate.year, startDate.month)
            ),
            "endDate" to InputFieldType("End Date", InputType.Date(endDate.year, endDate.month)),
            "description" to InputFieldType("Description", InputType.Text(description))
        )
    )

    class Experience(
        title: String,
        company: String,
        startDate: InputType.Date,
        endDate: InputType.Date,
        description: String
    ) : InputModel(
        fields = mapOf(
            "title" to InputFieldType("Title", InputType.Text(title)),
            "company" to InputFieldType("Company", InputType.Text(company)),
            "startDate" to InputFieldType(
                "Start Date",
                InputType.Date(startDate.year, startDate.month)
            ),
            "endDate" to InputFieldType("End Date", InputType.Date(endDate.year, endDate.month)),
            "description" to InputFieldType("Description", InputType.Text(description))
        )
    )

    class Achievement(
        title: String,
        description: String
    ) : InputModel(
        fields = mapOf(
            "title" to InputFieldType("Title", InputType.Text(title)),
            "description" to InputFieldType("Description", InputType.Text(description))
        )
    )

    class Education(
        institution: String,
        degree: String,
        startDate: InputType.Date,
        endDate: InputType.Date
    ) : InputModel(
        fields = mapOf(
            "institution" to InputFieldType("Institution", InputType.Text(institution)),
            "degree" to InputFieldType("Degree", InputType.Text(degree)),
            "startDate" to InputFieldType(
                "Start Date",
                InputType.Date(startDate.year, startDate.month)
            ),
            "endDate" to InputFieldType("End Date", InputType.Date(endDate.year, endDate.month))
        )
    )

    class ProfileLink(
        platform: String,
        url: String
    ) : InputModel(
        fields = mapOf(
            "platform" to InputFieldType("Platform", InputType.Text(platform)),
            "url" to InputFieldType("URL", InputType.Text(url))
        )
    )

    class ContactDetail(
        type: String,
        value: String
    ) : InputModel(
        fields = mapOf(
            "type" to InputFieldType("Type", InputType.Text(type)),
            "value" to InputFieldType("Value", InputType.Text(value))
        )
    )
}